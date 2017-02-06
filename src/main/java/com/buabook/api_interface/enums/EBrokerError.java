package com.buabook.api_interface.enums;

public enum EBrokerError {

	BADLY_FORMED_JSON {
		@Override
		public String getDescription() {
			return "Message was not a valid JSON object";
		}
	},
	COMMAND_MISSING {
		@Override
		public String getDescription() {
			return "Command key missing";
		}
	},
	ARGUMENTS_MISSING {
		@Override
		public String getDescription() {
			return "Arguments key missing or not a JSON object";
		}
	},
	INVALID_COMMAND {
		@Override
		public String getDescription() {
			return "Command not recognized. Valid commands: " + EBrokerCommands.valuesToStrings();
		}
	},
	MISSING_BOT_ID {
		@Override
		public String getDescription() {
			return "Missing unique client identifier";
		}
	},
	MISSING_REQUEST_ID {
		@Override
		public String getDescription() {
			return "Missing unique request ID";
		}
	}, 
	NO_ORDERS {
		@Override
		public String getDescription() {
			return "No valid game or season orders supplied";
		}
	},
	BAD_ARGUMENTS {
		@Override
		public String getDescription() {
			return "The arguments specified are invalid for the requested command";
		}
	};
	
	public abstract String getDescription();
}
