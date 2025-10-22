package jjfw.auth;

import org.json.JSONObject;

public class StatusResponse {

    private JSONObject jo;

    private StatusResponse(Builder b) {
        jo = new JSONObject();
        jo.append("status", b.status);
        jo.append("error", b.error);
    }

    @Override
    public String toString() {
        return jo.toString().replaceAll("\\[","").replaceAll("\\]","");
    }

    public static class Builder {

        private boolean error = false;
        private String status = "Unknown status";

        public static Builder newInstance() {
            return new Builder();
        }

        private Builder() {
        }

        // Setter methods
        public Builder withStatus(String s) {
            this.status = s;
            return this;
        }

        public Builder withError(boolean b) {
            this.error = b;
            return this;
        }

        // build method to deal with outer class
        // to return outer instance
        public StatusResponse build() {
            return new StatusResponse(this);
        }

    }
}