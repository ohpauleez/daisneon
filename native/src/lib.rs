#[macro_use]
extern crate neon;

use neon::vm::{Call, JsResult};
use neon::js::{JsFunction, JsString, JsNumber, JsArray, JsObject, JsValue, Object};
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

fn handle_chain(call: Call) -> JsResult<JsObject> {
    let scope = call.scope;
    let context = call.arguments.require(scope, 0)?.check::<JsObject>()?;
    let interceptors: Vec<Handle<JsValue>> = call.arguments.require(scope, 1)?.check::<JsArray>()?.to_vec(scope)?;
    let ctx = interceptors.iter()
                          .fold(context, |ctx, interceptor| {let i: Handle<JsObject> = interceptor.downcast::<JsObject>().unwrap();
                                                             let enter_f: Handle<JsFunction> = i.get(scope, "enter").unwrap().downcast::<JsFunction>().unwrap();
                                                             ctx});
    Ok(ctx)
}

register_module!(m, {
    m.export("hello", hello)?;
    m.export("returnObj", ret_obj)?;
    Ok(())
});

