package it.unipi.chessApp.repository;

import it.unipi.chessApp.dto.AverageEloResult;
import it.unipi.chessApp.dto.MonthlyOpeningStatDTO;
import it.unipi.chessApp.model.Game;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {

    // Note: only one opening is selected per month/year, in case of ex-equo a random one is selected
    @Aggregation(pipeline = {
        "{ '$match': { '$and': [ { 'end_time': { '$gte': ?2, '$lt': ?3 } }, { '$or': [ { 'white_rating': { '$gte': ?0 } }, { 'black_rating': { '$gte': ?1 } } ] } ] } }",
        "{ '$group': { '_id': { 'month': { '$month': { '$toDate': { '$multiply': ['$end_time', 1000] } } }, 'year': { '$year': { '$toDate': { '$multiply': ['$end_time', 1000] } } }, 'opening': '$opening' }, 'count': { '$sum': 1 } } }",
        "{ '$sort': { '_id.year': 1, '_id.month': 1, 'count': -1 } }",
        "{ '$group': { '_id': { 'month': '$_id.month', 'year': '$_id.year' }, 'most_used_opening': { '$first': '$_id.opening' }, 'usage_count': { '$first': '$count' } } }",
        "{ '$project': { '_id': 0, 'year': '$_id.year', 'month': '$_id.month', 'mostUsedOpening': '$most_used_opening', 'usageCount': '$usage_count' } }"
    })
    List<MonthlyOpeningStatDTO> getMonthlyTopOpenings(int minWhite, int minBlack, long startEpoch, long endEpoch);

    @Aggregation(pipeline = {
        "{ '$match': { 'opening': ?0, 'end_time': { '$gte': ?1, '$lt': ?2 } } }",
        "{ '$project': { 'avg_game_rating': { '$avg': ['$white_rating', '$black_rating'] } } }",
        "{ '$group': { '_id': null, 'finalAverageElo': { '$avg': '$avg_game_rating' } } }"
    })
    AverageEloResult getAverageEloForOpening(String opening, long startEpoch, long endEpoch);

    @Query("{'$or': [{'white_player': ?0}, {'black_player': ?0}]}")
    List<Game> findByPlayer(String username);
}
