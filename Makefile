# ============================================
# Project configuration (customize here)
# ============================================
GROUP_ID    ?= com.example
ARTIFACT_ID ?= karafusers
VERSION     ?= 1.0-SNAPSHOT
PACKAGE     ?= $(GROUP_ID).$(ARTIFACT_ID)

# ============================================
# Maven configuration
# ============================================
MVN        ?= mvn
MVN_FLAGS  ?= 

PROJECT_DIR ?= $(ARTIFACT_ID)

.DEFAULT_GOAL := build

.PHONY: help build package clean test verify install \
        run deps tree javadoc ci skip-tests

# ============================================
# Help
# ============================================
help:
	@echo "Available targets:"
	@echo "  build       Compile and package (default)"
	@echo "  clean       Remove target directory"
	@echo "  test        Run tests"
	@echo "  verify      Run mvn verify"
	@echo "  install     Install artifact to local repo"
	@echo "  run         Run application (requires exec plugin)"
	@echo "  deps        Download dependencies for offline use"
	@echo "  tree        Show dependency tree"
	@echo "  javadoc     Generate Javadoc"
	@echo "  ci          Clean and verify (CI mode)"
	@echo ""
	@echo "Current configuration:"
	@echo "  GROUP_ID=$(GROUP_ID)"
	@echo "  ARTIFACT_ID=$(ARTIFACT_ID)"
	@echo "  VERSION=$(VERSION)"

# ============================================
# Build lifecycle
# ============================================
build: package

package:
	$(MVN) $(MVN_FLAGS) package

clean:
	$(MVN) $(MVN_FLAGS) clean

test:
	$(MVN) $(MVN_FLAGS) test

verify:
	$(MVN) $(MVN_FLAGS) verify

install:
	$(MVN) $(MVN_FLAGS) install

skip-tests:
	$(MVN) $(MVN_FLAGS) -DskipTests verify

ci:
	$(MVN) $(MVN_FLAGS) clean verify

# ============================================
# Runtime
# ============================================
run:
	$(MVN) $(MVN_FLAGS) exec:java

# ============================================
# Dependencies
# ============================================
deps:
	$(MVN) $(MVN_FLAGS) dependency:go-offline

tree:
	$(MVN) $(MVN_FLAGS) dependency:tree

# ============================================
# Documentation
# ============================================
javadoc:
	$(MVN) $(MVN_FLAGS) javadoc:javadoc

