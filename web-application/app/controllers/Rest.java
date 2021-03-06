package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import models.*;
import play.db.jpa.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.*;

public class Rest extends Controller {

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result geotagged(Long matchId) {
        final List<Tweet> tweets = Tweet.geotagged(Match.findById(matchId));
        final JsonNode jsonNode = Json.toJson(tweets);
        return ok(jsonNode);
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result tweet(Long tweetId) {
        final Tweet tweet = Tweet.find(tweetId);
        final JsonNode jsonNode = Json.toJson(tweet);
        return ok(jsonNode).as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result topSentiment(Long matchId, String sentimentName) {
        final Match match = Match.findById(matchId);
        final Sentiment sentiment = Sentiment.valueOf(sentimentName.toUpperCase());
        final List<Tweet> tweets = Tweet.topWithSentiment(match, sentiment, 500);
        final JsonNode jsonNode = Json.toJson(tweets);
        return ok(jsonNode).as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result sentimentInTime(Long matchId) {
        final List<SentimentInTime> sentimentInTimeList = Tweet.getSentimentInTime(matchId);
        final JsonNode jsonNode = Json.toJson(sentimentInTimeList);
        return ok(jsonNode).as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result usersSentiment(Long matchId, String column) {
        final List<UserMatchSentiment> resultList = UserMatchSentiment.findTop(matchId, column);
        JsonNode jsonNode = Json.toJson(resultList);
        return ok(jsonNode).as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result topCountries(Long matchId) {
        Match match = Match.findById(matchId);
        final List<Geodata> resultList = Geodata.topCountries(match);
        JsonNode jsonNode = Json.toJson(resultList);
        return ok(jsonNode).as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result topStates(long matchId) {
        Match match = Match.findById(matchId);
        final List<Geodata> resultList = Geodata.topState(match);
        JsonNode jsonNode = Json.toJson(resultList);
        return ok(jsonNode).as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result topCounties(long matchId) {
        Match match = Match.findById(matchId);
        final List<Geodata> resultList = Geodata.topCounty(match);
        JsonNode jsonNode = Json.toJson(resultList);
        return ok(jsonNode).as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result topCities(long matchId) {
        Match match = Match.findById(matchId);
        final List<Geodata> resultList = Geodata.topCity(match);
        JsonNode jsonNode = Json.toJson(resultList);
        return ok(jsonNode).as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result matchCliques(long matchId) {
        final List<CliquesMatch> cliquesMatches = CliquesMatch.findForMatch(matchId);
        final JsonNode jsonNode = Json.toJson(cliquesMatches);
        return ok(jsonNode).as("application/json");
    }


    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result teamMatchStats(long teamId) {
        Team team = Team.findById(teamId);
        List<Match> matches = Match.find(team);
        List<MatchStats> matchStats = Lists.newArrayList();
        for(Match m : matches) {
            matchStats.add(MatchStats.findByMatchId(m.id, false));
        }
        return ok(Json.toJson(matchStats)).as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result teamCliques(long teamId) {
        final List<CliquesTeam> result = CliquesTeam.findForTeam(teamId);
        Collections.sort(result, Collections.reverseOrder(new Comparator<CliquesTeam>() {
            @Override
            public int compare(CliquesTeam o1, CliquesTeam o2) {
                return Double.compare(o1.positivness, o2.positivness);
            }
        }));
        return ok(Json.toJson(result)).as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result teamCliquesUsers(long teamId) {
        final List<CliquesTeam> cliquesTeam = CliquesTeam.findForTeam(teamId);
        final Set<CliquesTeamUser> result = CliquesTeamUser.findForTeam(cliquesTeam);
        return ok(Json.toJson(result)).as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result teamCliquesForUser(long userId) {
        final List<CliquesTeamUser> userCliques = CliquesTeamUser.findForUser(userId);
        final List<CliquesTeam> teamList = CliquesTeam.findForUser(userCliques);
        return ok(Json.toJson(teamList)).as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result sentimentUserOccurences(long teamId) {
        final List<UserOccurencesSentimentDTO> aggregatedForTeam = UserTeamStats.getAggregatedForTeam(teamId);
        return ok(Json.toJson(aggregatedForTeam)).as("application/json");
    }

    @BodyParser.Of(BodyParser.Json.class)
    @Transactional(readOnly = true)
    public static Result topGeodata(Long teamId, String column) {
        Team team = Team.findById(teamId);
        List<Match> matches = Match.find(team);
        final Map<Match, List<Geodata>> resultMap = Maps.newTreeMap(new Comparator<Match>() {
            @Override
            public int compare(Match o1, Match o2) {
                return o1.startDate.compareTo(o2.startDate);
            }
        });
        for(Match match : matches) {
            final List<Geodata> geodataList = Geodata.topGeodata(match, column);
            resultMap.put(match, geodataList);
        }
        JsonNode jsonNode = Json.toJson(resultMap);
        return ok(jsonNode).as("application/json");
    }
}
