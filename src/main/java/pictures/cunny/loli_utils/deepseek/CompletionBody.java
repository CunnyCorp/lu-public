package pictures.cunny.loli_utils.deepseek;

import java.util.ArrayList;
import java.util.List;

public class CompletionBody {
    public String model;
    public List<Message> messages = new ArrayList<>();
    public int max_tokens;
    public double temperature;

    public CompletionBody(Builder builder) {
        this.model = builder.model;
        for (String prompt : builder.sysPrompts) {
            this.messages.add(new Message("system", prompt));
        }

        this.messages.add(new Message("user", builder.userPrompt));
        this.max_tokens = builder.maxTokens;
        this.temperature = builder.temperature;
    }

    @SuppressWarnings("unused")
    public static class Builder {
        private String model = "deepseek-chat";
        private final List<String> sysPrompts = new ArrayList<>();
        private String userPrompt = "How do I tie a light bulb?";
        private int maxTokens = 32;
        private double temperature = 0;

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder sysPrompt(String prompt) {
            this.sysPrompts.add(prompt);
            return this;
        }

        public Builder userPrompt(String prompt) {
            this.userPrompt = prompt;
            return this;
        }

        public Builder maxTokens(int i) {
            this.maxTokens = i;
            return this;
        }

        public Builder temperature(double i) {
            this.temperature = i;
            return this;
        }

        public CompletionBody build() {
            return new CompletionBody(this);
        }
    }
}
