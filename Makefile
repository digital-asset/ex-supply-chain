.PHONY: build
build: build-dars build-ui

.PHONY: clean
clean:
	rm -rf .daml triggers/.daml
	rm -rf ui/node_modules
	rm -rf target

### DARS ###

MODELS_DAR=target/supplychain.dar
TRIGGERS_DAR=target/triggers.dar

.PHONY: build-dars
build-dars: $(MODELS_DAR) $(TRIGGERS_DAR)

DAML_SRC=$(shell find src/ -name '*.daml')

$(MODELS_DAR): $(DAML_SRC) daml.yaml
	daml build --output $@

TRIGGERS_DAML_SRC=$(shell find triggers/src/ -name '*.daml')

$(TRIGGERS_DAR): $(TRIGGERS_DAML_SRC) triggers/daml.yaml $(MODELS_DAR)
	daml build --project-root triggers --output ../$@


### JS Codegen ###

JS_CODEGEN_DIR=ui/daml.js
JS_CODEGEN_ARTIFACT=$(JS_CODEGEN_DIR)/supplychain-1.0.0/package.json

$(JS_CODEGEN_ARTIFACT): $(MODELS_DAR)
	daml codegen js -o $(JS_CODEGEN_DIR) $<


### UI Install ###

UI_INSTALL_ARTIFACT=ui/node_modules

$(UI_INSTALL_ARTIFACT): ui/package.json ui/yarn.lock $(JS_CODEGEN_ARTIFACT)
	cd ui && yarn install --force --frozen-lockfile

.PHONY: build-ui
build-ui: $(UI_INSTALL_ARTIFACT)
