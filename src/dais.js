
'use strict'

// The Dais interceptor chain in pure, naive JavaScript (using mutable data types)

function handleLeave(context) {
    let stack = context["dais.stack"];
    let stackLen = stack.length;
    for(let i = 0; i < stackLen; i++) {
        let interceptor = stack.pop();
        try {
            if (interceptor.leave !== undefined) {
                context = interceptor.leave(context);
            }
            if (context.error !== undefined) {
                return handleError(context);
            }
        } catch (e) {
            context["error"] = e;
            return handleError(context);
        }
    }
    return context;
}

function handleError(context) {
    let stack = context["dais.stack"];
    let stackLen = stack.length;
    for(let i = 0; i < stackLen; i++) {
        let error = context.error;
        if (error === undefined) {
            return handleLeave(context)
        }

        let interceptor = stack.pop();
        if (interceptor.error !== undefined) {
            context = interceptor.error(context);
        }
    }
    return context;
}

function handleEnter(context) {
    let queue = context["dais.queue"];
    let stack = context["dais.stack"];
    let terminators = context["dais.terminators"];
    while (queue.length > 0) {
        let interceptor = queue[0];
        if (interceptor === undefined) {
            delete context["dais.queue"];
            return handleLeave(context);
        }

        queue.shift();
        stack.push(interceptor);

        try {
            if (interceptor.enter !== undefined) {
                context = interceptor.enter(context);
            }
            if (context.error !== undefined) {
                return handleError(context);
            }
        } catch (e) {
            context["error"] = e;
            return handleError(context);
        }
        if (terminators !== undefined) {
            if (terminators.some((elem, i, array) => elem.call(this, context))) {
                delete context["dais.queue"];
                return handleLeave(context);
            }
        }
    }
    return context;
}

function execute(context, interceptors) {
    if (interceptors !== undefined) {
        context["dais.queue"] = interceptors;
    }
    context["dais.stack"] = context["dais.stack"] || [];
    return handleEnter(context);
}

module.exports.execute = execute;
module.exports.handleEnter = handleEnter;
module.exports.handleLeave = handleLeave;
module.exports.handleError = handleError;

