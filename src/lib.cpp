
#include <nan.h>
#include <algorithm>
#include <vector>
#include <list>

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
    auto global_js_context = Nan::GetCurrentContext()->Global();
    auto context = info[0]->ToObject();
    auto interceptors = info[1].As<v8::Array>();
    auto terminators_key = Nan::New("dais.terminators").ToLocalChecked();
    auto js_terminators = Nan::Has(context, terminators_key).FromJust() ? Nan::Get(context, terminators_key).ToLocalChecked().As<v8::Array>() : Nan::New<v8::Array>();
    std::vector<Local<v8::Function>> terminators;
    std::list<Local<v8::Function>> stack;

    auto enter_key = Nan::New("enter").ToLocalChecked();
    auto leave_key = Nan::New("leave").ToLocalChecked();
    auto should_process_leave = false; // A flag to signal if leave leave handlers should be processed

    //Prepare our terminators so we can iterate over them
    unsigned int term_len = 0;
    if (js_terminators->IsArray()) {
        term_len = js_terminators->Length();
    }
    for (unsigned int i = 0; i < term_len; i++) {
        if (Nan::Has(js_terminators, i).FromJust()) {
            auto terminator = Nan::Get(js_terminators, i).ToLocalChecked().As<v8::Function>();
            terminators.push_back(terminator);
        }
    }

    // Start processing interceptors
    unsigned int len = 0;
    if (interceptors->IsArray()) {
        len = interceptors->Length();
    }
    for (unsigned int i = 0; i < len; i++) {
        if (Nan::Has(interceptors, i).FromJust()) {
            // Get the interceptor object
            auto interceptor = Nan::Get(interceptors, i).ToLocalChecked().As<v8::Object>();
            // If the interceptor has a `leave`, add it to the stack
            if (Nan::Has(interceptor, leave_key).FromJust()) {
                stack.push_front(Nan::Get(interceptor, leave_key).ToLocalChecked().As<v8::Function>());
            }
            // If the interceptor has an `enter`, call it.  Otherwise, move on to the next interceptor
            if (Nan::Has(interceptor, enter_key).FromJust()) {
                auto enter_fn = Nan::Get(interceptor, enter_key).ToLocalChecked().As<v8::Function>();
                v8::Local<v8::Value> args[] = {context.As<v8::Value>()};
                context = Nan::Call(enter_fn, global_js_context, 1, args).ToLocalChecked().As<v8::Object>();
                // Check terminators
                if (std::any_of(terminators.cbegin(), terminators.cend(),
                            [&](Local<v8::Function> terminator) {
                                return Nan::To<bool>(Nan::Call(terminator, global_js_context, 1, args).ToLocalChecked()).FromJust();
                            })) {
                    should_process_leave = true;
                    break;
                }
            }
        }
    }
    // Process Leave
    if (should_process_leave) {
        for (auto const &leave_fn : stack) {
            v8::Local<v8::Value> args[] = {context.As<v8::Value>()};
            context = Nan::Call(leave_fn, global_js_context, 1, args).ToLocalChecked().As<v8::Object>();
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

