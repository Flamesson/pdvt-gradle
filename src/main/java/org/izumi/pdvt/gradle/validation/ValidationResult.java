package org.izumi.pdvt.gradle.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ValidationResult {
    private final List<String> messages = new ArrayList<>(4);
    private final boolean valid;

    public ValidationResult(boolean valid) {
        this.valid = valid;
    }

    public static ValidationResult success() {
        return new ValidationResult(true);
    }

    public static ValidationResult failure() {
        return new ValidationResult(false);
    }

    /**
     * <p>Выполняет переданные функции до тех пор, пока не встретится невалидный результат.</p>
     *
     * <p>Все сообщения, включая сообщения последней выполненной функции, попадают в результат.</p>
     *
     * @param suppliers функции для выполнения.
     * @return Валидный результат с набором полученных сообщений если все переданные функции вернули
     *     валидный результат. Невалидный результат с набором полученных сообщений до (включая) первой функции,
     *     вернувшей невалидный результат.
     */
    @SafeVarargs
    public static ValidationResult untilFirstFail(Supplier<ValidationResult>... suppliers) {
        final var probablyResult = ValidationResult.success();
        for (Supplier<ValidationResult> supplier : suppliers) {
            final var supplied = supplier.get();

            if (supplied.isNotValid()) {
                return ValidationResult.failure()
                        .addMessages(probablyResult.messages)
                        .addMessages(supplied.messages);
            }

            probablyResult.addMessages(supplied.messages);
        }

        return probablyResult;
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isNotValid() {
        return !valid;
    }

    public ValidationResult addMessage(String message) {
        messages.add(message);
        return this;
    }

    /**
     * <p>Добавляет сообщение, если проверка не пройдена. Возвращает себя.
     *     Если проверка пройдена - просто возвращает себя, без добавления сообщения.</p>
     *
     * @param message Сообщение.
     * @return Себя.
     */
    public ValidationResult addMessageIfNotValid(String message) {
        if (isValid()) {
            return this;
        }

        return addMessage(message);
    }

    public ValidationResult addMessages(Collection<String> messages) {
        this.messages.addAll(messages);
        return this;
    }

    /**
     * <p>Создаёт новый объект.</p>
     *
     * <p>Работает точно так же, как и логическое "И".</p>
     *
     * </p>Сообщения об ошибках суммируются.</p>
     */
    public ValidationResult and(ValidationResult result) {
        final ValidationResult joined;
        if (isValid() && result.isValid()) {
            joined = ValidationResult.success();
        } else {
            joined = ValidationResult.failure();
        }

        joined.addMessages(this.messages);
        joined.addMessages(result.messages);

        return joined;
    }

    public String joinMessagesAndGetResult(Collector<CharSequence, ?, String> collector) {
        return messages.stream().collect(collector);
    }

    public String joinMessagesAndGetResult(String delimiter) {
        return joinMessagesAndGetResult(Collectors.joining(delimiter));
    }

    public void joinMessagesAndCall(Consumer<String> callback) {
        joinMessagesAndCall(System.lineSeparator(), callback);
    }

    public void joinMessagesAndCall(String delimiter, Consumer<String> callback) {
        callback.accept(joinMessagesAndGetResult(delimiter));
    }

    /**
     * <p>Выбросить {@link RuntimeException}, если результат валидации отрицательный.</p>
     *
     * @param constructor Функция создания объекта ошибки, которую нужно выбросить.
     * @param delimiter   Разделитель, который нужно поместить между хранимыми сообщениями.
     * @param <E>         Тип ошибки. Рекомендовано делать его более специфичным для уточнения контекста.
     */
    public <E extends RuntimeException> void throwIfFailure(Function<String, E> constructor, String delimiter) {
        if (isNotValid()) {
            throw constructor.apply(joinMessagesAndGetResult(delimiter));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValidationResult that = (ValidationResult) o;
        return valid == that.valid && Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messages, valid);
    }
}
