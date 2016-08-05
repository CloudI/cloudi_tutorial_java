#
# Makefile for Book Recommendation System using CloudI
#

# Adjust this for the name of the Cloudi service name
ERL_MODULE=book.erl 
MODULE_CONF=book.conf
PATH_CONF=path.conf

#
# Configuration Settings
#
ERLC=/usr/bin/erlc
ERLCFLAGS=-o
BASE_DIR=/opt/book/service
EBIN_DIR=$(BASE_DIR)/ebin
DOC_DIR=$(BASE_DIR)/edoc
SRC_DIR=$(BASE_DIR)

CLOUDILIBS= -pz /usr/local/lib/cloudi-1.5.0/lib/cloudi_core-1.5.0 -pz /usr/local/lib/cloudi-1.5.0/lib/cloudi_core-1.5.0/ebin 
DIALYZERLIBS= -pa /usr/local/lib/cloudi-1.5.0/lib/cloudi_core-1.5.0 -pa /usr/local/lib/cloudi-1.5.0/lib/cloudi_core-1.5.0/ebin 

CLOUDI_HTTP=http://localhost:6467/cloudi/api/erlang
BOOK_HTTP=http://localhost:6467/recommend/book


#
# Verify that the things we need to run are available on this system
#
ERL = $(shell which erl)

ifeq ($(ERL),)
	$(error "Erlang not available")
endif

cloudi_nodes := $(shell sudo cloudi ping)


#define start-cloudi =
#	sudo cloudi start
#	touch cloudi_started.flag
#endef

#define stop-cloudi =
#	sudo cloudi stop
#	touch cloudi_stopped.flag
#endef

#
# Rules
#

all: setup-dirs compile 

#deps:

setup-dirs:
	mkdir -p $(EBIN_DIR)
	mkdir -p $(DOC_DIR)
	echo '"$(EBIN_DIR)"' > $(PATH_CONF)


compile: $(ERL_MODULE) 
	$(ERLC) $(ERLCFLAGS) $(EBIN_DIR) $(CLOUDILIBS) $(SRC_DIR)/$(ERL_MODULE)

doc:
	erl -noshell -run edoc_run packages '[""]' '[{source_path, ["."]}, {dir, "edoc"}]'

check:  compile
	dialyzer $(DIALYZERLIBS) $(SRC_DIR)/$(ERL_MODULE)

clean:
	touch cloudi_started.flag
	touch cloudi_stopped.flag

rebuild: setup-dirs compile check doc

deploy: cloudi-stop cloudi-start service-add

cloudi-start: cloudi_stopped.flag
	sudo cloudi start
	touch cloudi_started.flag
	sleep 10
	
cloudi-stop: cloudi_started.flag
	sudo cloudi stop
	touch cloudi_stopped.flag

service-add: compile 
	curl -X POST -d @path.conf $(CLOUDI_HTTP)/code_path_add
	curl -X POST -d @$(MODULE_CONF) $(CLOUDI_HTTP)/services_add

services: 
	curl $(CLOUDI_HTTP)/services

code_path:
	curl $(CLOUDI_HTTP)/code_path 

test: 
#	curl $(BOOK_HTTP)/newbooks
	curl $(BOOK_HTTP)/popularbooks
#	curl $(BOOK_HTTP)/recommendedbooks?user=1
#	curl $(BOOK_HTTP)/allbooks?id=47675
#	curl -X POST -d "item=45388" $(BOOK_HTTP) 

	
