
#include <nan.h>

using namespace v8;

// NAN_METHOD is a Nan macro enabling convenient way of creating native node functions.
// It takes a method's name as a param. By C++ convention, I used the Capital cased name.
NAN_METHOD(Hello) {
    // Create an instance of V8's String type
    auto message = Nan::New("hello node - from C++").ToLocalChecked();
    // 'info' is a macro's "implicit" parameter - it's a bridge object between C++ and JavaScript runtimes
    // You would use info to both extract the parameters passed to a function as well as set the return value.
    info.GetReturnValue().Set(message);
}

NAN_METHOD(BasicExecute) {
    auto context = info[0]->ToObject();
    auto interceptors = info[1].As<v8::Array>();
    auto enter_key = Nan::New("enter").ToLocalChecked();

    int len = 0;
    if (interceptors->IsArray()) {
        len = interceptors->Length();
    }
    for (int i = 0; i < len; i++) {
        if (Nan::Has(interceptors, i).FromJust()) {
            auto interceptor_v = Nan::Get(interceptors, i).ToLocalChecked();
            auto interceptor = Nan::To<v8::Object>(interceptor_v).ToLocalChecked();
            auto enter_fn = Nan::Get(interceptor, enter_key).ToLocalChecked().As<v8::Function>();
            v8::Local<v8::Value> args[] = {context.As<v8::Value>()};
            context = Nan::Call(enter_fn, Nan::GetCurrentContext()->Global(), 1, args).ToLocalChecked().As<v8::Object>();
        }
    }
    info.GetReturnValue().Set(context);
}

// Module initialization logic
NAN_MODULE_INIT(Initialize) {
    // Export the `Hello` function (equivalent to `export function Hello (...)` in JS)
    NAN_EXPORT(target, Hello);
    NAN_EXPORT(target, BasicExecute);
}

// Create the module called "addon" and initialize it with `Initialize` function (created with NAN_MODULE_INIT macro)
NODE_MODULE(cppaddon, Initialize);

