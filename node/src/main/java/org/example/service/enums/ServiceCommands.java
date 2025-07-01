package org.example.service.enums;

public enum ServiceCommands {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start"),
    HISTORY("/history"),
    HISTORY_RANGE("/historyRange");
    private final String value;

    ServiceCommands(String value) {
        this.value = value;
    }


    @Override
    public String toString() {
        return value;
    }

    public boolean equals(String cmd) {
        return this.toString().equals(cmd);
    }

    public static Object fromValue(String textMessage) {
        for (ServiceCommands cmd : ServiceCommands.values()) {
            if (cmd.value.equals(textMessage)) {
                return cmd;
            }
        }
        return null;
    }
}
