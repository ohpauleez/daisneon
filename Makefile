
SHELL := /usr/bin/env bash

# This path is only accurate when calling with `make -f` directly
# In a shell-wrap situation it resolves to the temp file descriptor
MAKEFILE_PATH := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))

RUN_ARGS := $(wordlist 2,$(words $(MAKECMDGOALS)),$(MAKECMDGOALS))

# Tools/Executables
LUMO ?=lumo
LUMO_BUILD_FILE ?=build.cljs
NEON ?=neon
NODE_GYP ?=node-gyp
NPM ?=npm

# Auxiliary Functions
quiet = $(if $V, $1, @echo " $2"; $1)
very-quiet = $(if $V, $1, @$1)

#define cljscompilefn
#$(shell $(LUMO) -c $1 $(LUMO_BUILD_FILE) $2)
#endef

## TODO: Use vars/alias/functions from above for all build steps

.DEFAULT_GOAL := local-install

.PHONY : all
all: local-install

.PHONY : cljs-repl
cljs-repl:
	npm run cljs-socketrepl

.PHONY : cljs
cljs:
	npm run compile-cljs

.PHONY : rust
rust:
	npm run compile-rust

.PHONY : cpp
cpp:
	npm run compile-cpp

.PHONY : local-install
local-install:
	npm install

.PHONY : clean
clean:
	rm -r ./build ./target ./native/target ./native/index.node

