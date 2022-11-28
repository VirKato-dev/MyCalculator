package calc2;

import java.util.Arrays;
import java.util.stream.Stream;


public class Main {

    public static void main(String[] args) {
        System.out.println(calc("2 + 2"));
        System.out.println(calc("V - I"));
        System.out.println(calc("III * IV"));
//        System.out.println(calc("3 * IV"));
//        System.out.println(calc("IV / 2"));
//        System.out.println(calc("0 / 2"));
//        System.out.println(calc("120 / 4"));
//        System.out.println(calc("4 / / 2"));
//        System.out.println(calc("4 / 2 * 3")); // при максимальном количестве действий = 2
//        System.out.println(calc("4 /"));
//        System.out.println(calc("4 2"));
//        System.out.println(calc("/ 2 1"));
    }

    public static String calc(final String input) {
        Expression expr = new Expression(input);
        expr.calculate();
        return input + " = " + expr.getResult();
    }
}


enum TokenType {NONE, ARABIC, ROMAN, ACTION}


record Token(TokenType type, String token) {
}


enum ROMAN { NAN,
    I, II, III, IV, V, VI, VII, VIII, IX, X,
    XI, XII, XIII, XIV, XV, XVI, XVII, XVIII, XIX, XX,
    XXI, XXII, XXIII, XXIV, XXV, XXVI, XXVII, XXVIII, XXIX, XXX,
    XXXI, XXXII, XXXIII, XXXIV, XXXV, XXXVI, XXXVII, XXXVIII, XXXIX, XL,
    XLI, XLII, XLIII, XLIV, XLV, XLVI, XLVII, XLVIII, XLIX, L,
    LI, LII, LIII, LIV, LV, LVI, LVII, LVIII, LIX, LX,
    LXI, LXII, LXIII, LXIV, LXV, LXVI, LXVII, LXVIII, LXIX, LXX,
    LXXI, LXXII, LXXIII, LXXIV, LXXV, LXXVI, LXXVII, LXXVIII, LXXIX, LXXX,
    LXXXI, LXXXII, LXXXIII, LXXXIV, LXXXV, LXXXVI, LXXXVII, LXXXVIII, LXXXIX, XC,
    XCI, XCII, XCIII, XCIV, XCV, XCVI, XCVII, XCVIII, XCIX, C
}


final class Expression {
    private static final Token NAV = new Token(TokenType.NONE, null);
    private static final String ACTIONS = "+-*/";
    private final Stream<String> TOKENS;
    private int maxActionsPerExpression = 1;
    private byte phase = 0;
    private Token action, result;

    public Expression(final String input) {
        TOKENS = Arrays.stream(input.replaceAll("\s+", " ").split(" "));
    }

    /***
     * Расчёт
     */
    public void calculate() {
        result = TOKENS.map(this::asToken)
                .reduce((acc, next) -> { // первый элемент уже в acc
                    switch (phase) {
                        // каждый чётный элемент - действие
                        case 0 -> action = (next.type() == TokenType.ACTION) ? next : NAV;
                        // каждый нечётный элемент запускает расчёт
                        case 1 -> acc = (next.type() != TokenType.ACTION) ? doAction(acc, next) : NAV;
                    }
                    phase = (byte) ((phase + 1) % 2);
                    return acc;
                }).orElse(NAV);
    }

    /***
     * Получить результат
     * @return текстовое представление результата
     */
    public String getResult() {
        if (phase != 0) throw new RuntimeException("Структура выражения нарушена");
        return result.token();
    }

    private Token doAction(Token num1, Token num2) {
        int op1, op2;

        //приведение к виду удобному для расчётов
        if (num1.type() == TokenType.ARABIC) {
            op1 = Integer.parseInt(num1.token());
            op2 = Integer.parseInt(num2.token());
        } else {
            op1 = ROMAN.valueOf(num1.token()).ordinal();
            op2 = ROMAN.valueOf(num2.token()).ordinal();
        }

        if (op1 < 1 || op2 < 1 || op1 > 10 || op2 > 10)
            throw new RuntimeException("Нарушен предел входного значения");

        int result = switch (action.token()) {
            case "+" -> op1 + op2;
            case "-" -> op1 - op2;
            case "*" -> op1 * op2;
            case "/" -> op1 / op2;
            default -> 0;
        };

        decActionCount();

        //приведение числа к виду требуемому на вывод
        if (num1.type() == TokenType.ARABIC) {
            return new Token(TokenType.ARABIC, String.valueOf(result));
        } else {
            if (result < 1) throw new RuntimeException();
            return new Token(TokenType.ROMAN, ROMAN.values()[result].name());
        }
    }

    /***
     * Уменьшить счётчик допустимых операций
     */
    private void decActionCount() {
        if (--maxActionsPerExpression < 0)
            throw new RuntimeException("Нарушено количество допустимых операций в выражении");
    }

    /***
     * Определить смысловой тип токена
     * @param word представление токена
     * @return запись
     */
    private Token asToken(String word) {
        TokenType type;
        try {
            type = TokenType.ROMAN;
            ROMAN.valueOf(word);
        } catch (Exception ignore1) {
            try {
                type = TokenType.ARABIC;
                Integer.parseInt(word);
            } catch (Exception ignore2) {
                try {
                    type = TokenType.ACTION;
                    ACTIONS.contains(word);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return new Token(type, word);
    }
}
