DAML_SRC=$(shell find src/ -name '*.daml')
TS_SRC=$(shell find ui/src/ -name '*.ts*')

DAR=.daml/dist/supplychain-1.0.0.dar
JS_CODEGEN_DIR=daml.js
JS_CODEGEN_ARTIFACT=$(JS_CODEGEN_DIR)/supplychain-1.0.0/package.json
UI_INSTALL_ARTIFACT=ui/node_modules
UI_BUILD_ARTIFACT=ui/build/index.html
UI_DEPLOY_ARTIFACT=deploy/ui.zip

$(DAR): $(DAML_SRC)
	daml build --output $@

$(JS_CODEGEN_ARTIFACT): $(DAR)
	daml codegen js -o $(JS_CODEGEN_DIR) $<

$(UI_INSTALL_ARTIFACT): ui/package.json ui/yarn.lock $(JS_CODEGEN_ARTIFACT)
	cd ui && yarn install --force --frozen-lockfile

build: $(DAR) $(UI_INSTALL_ARTIFACT)

$(UI_BUILD_ARTIFACT): $(UI_INSTALL_ARTIFACT) $(TS_SRC)
	cd ui && yarn build

$(UI_DEPLOY_ARTIFACT): $(UI_BUILD_ARTIFACT)
	mkdir -p deploy
	cd ui && zip -r ../$(UI_DEPLOY_ARTIFACT) build

deploy: $(DAR) $(UI_DEPLOY_ARTIFACT)
	cp $< deploy/

clean:
	rm -rf .daml
	rm -rf daml.js
	rm -rf ui/node_modules
	rm -rf ui/build
	rm -rf deploy
	rm -rf target

.PHONY: build deploy clean
