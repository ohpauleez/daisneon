#[macro_use]
extern crate neon;
extern crate itertools;

use itertools::Itertools;
use itertools::FoldWhile::{Continue, Done};

use neon::vm::{Call, JsResult};
use neon::js::{JsFunction, JsString, JsNumber, JsBoolean, JsNull, JsArray, JsObject, JsValue, Object};
use neon::mem::Handle;

fn hello(call: Call) -> JsResult<JsString> {
    let scope = call.scope;
    Ok(JsString::new(scope, "hello node - from Rust").unwrap())
}

fn ret_obj(call: Call) -> JsResult<JsObject> {
    let scope = call.scope;
    let obj: Handle<JsObject> = JsObject::new(scope);
    obj.set("one", JsNumber::new(scope, 1_f64))?;
    Ok(obj)
}

fn basic_execute(call: Call) -> JsResult<JsObject> {
    let scope = call.scope;
    let context = call.arguments.require(scope, 0)?.check::<JsObject>()?;
    let mut stack : Vec<Handle<JsObject>> = Vec::new();
    let mut did_terminate = false;
    let interceptors: Vec<Handle<JsValue>> = call.arguments.require(scope, 1)?.check::<JsArray>()?.to_vec(scope)?;
    let ectx = interceptors.iter()
                           .fold_while(context, |ctx, interceptor| {
                               let i: Handle<JsObject> = interceptor.downcast::<JsObject>().unwrap();
                               stack.push(i);
                               // TODO: We need to do a `match` on enter_f in case the interceptor
                               // doesn't have it
                               let enter_f: Handle<JsFunction> = i.get(scope, "enter").unwrap().downcast::<JsFunction>().unwrap();
                               let new_ctx: Handle<JsObject> = enter_f.call(scope, JsNull::new(), vec![ctx]).unwrap().check::<JsObject>().unwrap();
                               let terminators: Vec<Handle<JsValue>> = ctx.get(scope, "dais.terminators").unwrap().check::<JsArray>().unwrap().to_vec(scope).unwrap();
                               let should_terminate = terminators.iter()
                                   .any(|term_val| {
                                       let terminator: Handle<JsFunction> = term_val.downcast::<JsFunction>().unwrap();
                                       let term_res: Handle<JsBoolean> = terminator.call(scope, JsNull::new(), vec![ctx]).unwrap().check::<JsBoolean>().unwrap();
                                       term_res.value()});
                               if should_terminate { did_terminate = true; Done(new_ctx) } else { Continue(new_ctx) }}).into_inner();

    stack.reverse();
    let final_ctx = if did_terminate {
                      stack.iter()
                           .fold(ectx, |ctx, interceptor| {
                               let maybe_leave = interceptor.get(scope, "leave").unwrap().downcast::<JsFunction>();
                               match maybe_leave {
                                   Some(leave_f) => leave_f.call(scope, JsNull::new(), vec![ctx]).unwrap().check::<JsObject>().unwrap(),
                                   None => ctx,
                               }})
                    } else { ectx };
    Ok(final_ctx)
    //Ok(if did_terminate { handle_leave(&call, &mut ctx, &stack) } else { ctx })
}

register_module!(m, {
    m.export("hello", hello)?;
    m.export("returnObj", ret_obj)?;
    m.export("basicExecute", basic_execute)?;
    Ok(())
});

