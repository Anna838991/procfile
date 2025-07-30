package tutorial;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.CopyMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

public class Bot extends TelegramLongPollingBot {
    private InlineKeyboardMarkup keyboardM1;
    private InlineKeyboardMarkup keyboardM2;

    private final String token;

    public Bot() {
        token = System.getenv("");
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("❌ BOT_TOKEN переменная окружения не установлена!");
        }
    }

    @Override
    public String getBotUsername() {
        return "AnnaBaxBot";
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasCallbackQuery()) {
                CallbackQuery callback = update.getCallbackQuery();
                String data = callback.getData();
                Long chatId = callback.getMessage().getChatId();
                int msgId = callback.getMessage().getMessageId();
                String queryId = callback.getId();
                buttonTap(chatId, queryId, data, msgId);
                return;
            }

            if (update.hasMessage() && update.getMessage().hasText()) {
                Message msg = update.getMessage();
                String text = msg.getText();
                Long chatId = msg.getChatId();

                switch (text) {
                    case "/start":
                        initInlineKeyboards();
                        SendMessage sm = SendMessage.builder()
                                .chatId(chatId.toString())
                                .text("Привет! Это главное меню (inline):")
                                .replyMarkup(keyboardM1)
                                .build();
                        execute(sm);
                        break;
                    case "/menu":
                        sendTextWithKeyboard(chatId, "Выберите пункт меню:", getReplyKeyboard());
                        break;
                    case "Главное меню":
                        sendText(chatId, "Это главное меню.");
                        break;
                    case "О нас":
                        sendText(chatId, "Мы — команда разработчиков Telegram-ботов.");
                        break;
                    case "Контакты":
                        sendText(chatId, "Связаться с нами: https://t.me/Baxa_A");
                        break;
                    default:
                        sendText(chatId, "Вы написали: " + text);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initInlineKeyboards() {
        InlineKeyboardButton next = InlineKeyboardButton.builder().text("➡ Далее").callbackData("next").build();
        InlineKeyboardButton back = InlineKeyboardButton.builder().text("⬅ Назад").callbackData("back").build();

        List<List<InlineKeyboardButton>> rows1 = new ArrayList<>();
        rows1.add(List.of(next));
        keyboardM1 = InlineKeyboardMarkup.builder().keyboard(rows1).build();

        List<List<InlineKeyboardButton>> rows2 = new ArrayList<>();
        rows2.add(List.of(back));
        keyboardM2 = InlineKeyboardMarkup.builder().keyboard(rows2).build();
    }

    private ReplyKeyboardMarkup getReplyKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add("Главное меню");
        row1.add("О нас");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("Контакты");

        rows.add(row1);
        rows.add(row2);

        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    private void buttonTap(Long id, String queryId, String data, int msgId) throws TelegramApiException {
        EditMessageText newTxt = EditMessageText.builder()
                .chatId(id.toString())
                .messageId(msgId)
                .text("")
                .build();

        EditMessageReplyMarkup newKb = EditMessageReplyMarkup.builder()
                .chatId(id.toString())
                .messageId(msgId)
                .build();

        if (data.equals("next")) {
            newTxt.setText("📋 Вы перешли в меню 2");
            newKb.setReplyMarkup(keyboardM2);
        } else if (data.equals("back")) {
            newTxt.setText("🔙 Вы вернулись в меню 1");
            newKb.setReplyMarkup(keyboardM1);
        }

        AnswerCallbackQuery close = AnswerCallbackQuery.builder().callbackQueryId(queryId).build();
        execute(close);
        execute(newTxt);
        execute(newKb);
    }

    public void sendText(Long who, String what) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what)
                .build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendTextWithKeyboard(Long who, String what, ReplyKeyboardMarkup keyboard) {
        SendMessage sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what)
                .replyMarkup(keyboard)
                .build();
        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void copyMessage(Long who, Integer msgId) {
        CopyMessage cm = CopyMessage.builder()
                .fromChatId(who.toString())
                .chatId(who.toString())
                .messageId(msgId)
                .build();
        try {
            execute(cm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        Bot bot = new Bot();
        botsApi.registerBot(bot);
        System.out.println("🤖 Бот запущен успешно!");
    }
}

