MODELS_DAR=target/supplychain.dar
TRIGGERS_DAR=target/supplychain-triggers.dar
JS_CODEGEN_DIR=ui/daml.js

.PHONY: build
build: build-dars yarn-install-deps

.PHONY: clean
clean:
	rm -rf .daml triggers/.daml
	rm -rf ui/node_modules ui/build $(JS_CODEGEN_DIR)
	rm -rf target

### DARS ###

.PHONY: build-dars
build-dars: $(MODELS_DAR) $(TRIGGERS_DAR)

DAML_SRC=$(shell find src/ -name '*.daml')

$(MODELS_DAR): $(DAML_SRC) daml.yaml
	daml build --output $@

TRIGGERS_DAML_SRC=$(shell find triggers/src/ -name '*.daml')

$(TRIGGERS_DAR): $(TRIGGERS_DAML_SRC) triggers/daml.yaml $(MODELS_DAR)
	cd triggers && daml build --output ../$@

.PHONY: test-dars
test-dars: build-dars
	daml test --junit target/daml-test-reports/model.xml
	cd triggers && daml test --junit ../target/daml-test-reports/triggers.xml


### JS Codegen ###

JS_CODEGEN_ARTIFACT=$(JS_CODEGEN_DIR)/supplychain-1.0.0/package.json

$(JS_CODEGEN_ARTIFACT): $(MODELS_DAR)
	daml codegen js -o $(JS_CODEGEN_DIR) $<


### UI Install ###

UI_INSTALL_ARTIFACT=ui/node_modules

$(UI_INSTALL_ARTIFACT): ui/package.json ui/yarn.lock $(JS_CODEGEN_ARTIFACT)
	cd ui && yarn install --force --frozen-lockfile

yarn-install-deps: $(UI_INSTALL_ARTIFACT)

.PHONY: package
package: yarn-install-deps
	cd ui && \
     REACT_APP_HTTP_BASE_URL=https://$(LEDGER_ID).projectdabl.com \
      REACT_APP_WS_BASE_URL=wss://$(LEDGER_ID).projectdabl.com \
	   yarn build && mkdir -p ../target && zip -r ../target/supplychain.zip build/
