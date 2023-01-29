package com.expiredminotaur.bcukbot.fun.trivia.api;

import com.expiredminotaur.bcukbot.HttpHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.net.URL;

@Component
public class TriviaAPI
{
    private final Logger logger = LoggerFactory.getLogger(TriviaAPI.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private String token;

    public TriviaAPI()
    {
        getSessionToken();
    }

    public void getSessionToken()
    {
        try(BufferedReader br = HttpHandler.textRequest(new URL("https://opentdb.com/api_token.php?command=request")))
        {
            String output;
            if ((output = br.readLine()) != null)
            {
                SessionToken tokenRequest = gson.fromJson(output, SessionToken.class);
                if (tokenRequest.getResponseCode() == 0)
                {
                    token = tokenRequest.getToken();
                } else
                {
                    logger.error(String.format("Failed to get Trivia API token, Error code %d", tokenRequest.getResponseCode()));
                }
                return;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        logger.error("Failed to get Trivia API token, no response");
    }

    public Questions.Question getQuestion() throws Exception
    {
        Questions.Question question = null;
        try(BufferedReader br = HttpHandler.textRequest(new URL(String.format("https://opentdb.com/api.php?amount=1&token=%s", token))))
        {
            String output;
            if ((output = br.readLine()) != null)
            {
                Questions questionsRequest = gson.fromJson(output, Questions.class);
                question = processQuestionResponse(questionsRequest);
            }
        }
        if (question == null) logger.error("Failed to get Trivia question, no response");
        return question;
    }

    private Questions.Question processQuestionResponse(Questions questionsRequest) throws Exception
    {
        switch (questionsRequest.getResponseCode())
        {
            case 0:
                return questionsRequest.getQuestions().get(0);
            case 1:
                logger.error("No questions available");
                return null;
            case 2:
                logger.error("Invalid question request");
                return null;
            case 3:
                logger.error("API token not found");
                getSessionToken();
                return getQuestion();
            case 4:
                logger.error("Token out of questions");
                resetSessionToken();
                return getQuestion();
            default:
                logger.error("Unknown response code");
                return null;
        }
    }

    private void resetSessionToken() throws Exception
    {
        try (BufferedReader br = HttpHandler.textRequest(new URL(String.format("https://opentdb.com/api_token.php?command=reset&token=%s", token))))
        {
            String output;
            if ((output = br.readLine()) != null)
            {
                SessionToken tokenRequest = gson.fromJson(output, SessionToken.class);
                if (tokenRequest.getResponseCode() == 0)
                {
                    token = tokenRequest.getToken();
                } else
                {
                    logger.error(String.format("Failed to reset Trivia API token, Error code %d", tokenRequest.getResponseCode()));
                }
                return;
            }
        }
        logger.error("Failed to reset Trivia API token, no response");
    }
}
