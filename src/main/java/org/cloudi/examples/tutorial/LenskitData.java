//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:

package org.cloudi.examples.tutorial;

import java.util.List;
import java.util.LinkedList;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.core.LenskitRecommenderEngine;
import org.grouplens.lenskit.core.ModelDisposition;
import org.grouplens.lenskit.core.RecommenderConfigurationException;
import org.grouplens.lenskit.data.sql.JDBCRatingDAO;
import org.grouplens.lenskit.data.sql.JDBCRatingDAOBuilder;
import org.grouplens.lenskit.knn.MinNeighbors;
import org.grouplens.lenskit.knn.NeighborhoodSize;
import org.grouplens.lenskit.knn.item.ItemItemScorer;
import org.grouplens.lenskit.knn.item.ModelSize;
import org.grouplens.lenskit.knn.item.ItemSimilarity;
import org.grouplens.lenskit.knn.item.ItemSimilarityThreshold;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.vectors.similarity.CosineVectorSimilarity;
import org.grouplens.lenskit.vectors.similarity.VectorSimilarity;
import org.grouplens.lenskit.vectors.similarity.SimilarityDamping;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.baseline.UserMeanBaseline;
import org.grouplens.lenskit.baseline.ItemMeanRatingItemScorer;
import org.grouplens.lenskit.baseline.UserMeanItemScorer;
import org.grouplens.lenskit.transform.threshold.AbsoluteThreshold;
import org.grouplens.lenskit.transform.threshold.ThresholdValue;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.BaselineSubtractingUserVectorNormalizer;

public class LenskitData
{
    public static final double RATING_MIN = 0.5;
    public static final double RATING_MAX = 5.0;

    private final LenskitRecommenderEngine engine;

    public LenskitData(Connection db)
        throws RecommenderBuildException
    {
        // based on http://lenskit.org/documentation/basics/data-access/
        LenskitConfiguration config = LenskitData.configuration();
        JDBCRatingDAO dao = LenskitData.ratingsDAO(db);
        LenskitConfiguration data_config = new LenskitConfiguration();
        data_config.addComponent(dao);
        this.engine =
            LenskitRecommenderEngine.newBuilder()
                                    .addConfiguration(config)
                                    .addConfiguration(data_config,
                                                      ModelDisposition.EXCLUDED)
                                    .build();
    }

    private final LenskitRecommender recommenderSession(final Connection db)
        throws RecommenderConfigurationException
    {
        // based on http://lenskit.org/documentation/basics/data-access/
        JDBCRatingDAO dao = LenskitData.ratingsDAO(db);
        LenskitConfiguration data_config = new LenskitConfiguration();
        data_config.addComponent(dao);
        return this.engine.createRecommender(data_config);
    }

    public final JSONResponse itemList(final Connection db,
                                       final long user_id,
                                       final String language)
    {
        PreparedStatement select = null;
        ResultSet select_result = null;
        LinkedList<JSONItem> output = new LinkedList<JSONItem>();
        boolean db_failure = false;
        try
        {
            // select all items with the user's ratings
            select = db.prepareStatement(
                "SELECT items.id AS item_id, " +
                       "items.creator, " +
                       "items.creator_link, " +
                       "items.title, " +
                       "items.date_created, " +
                       "items.languages, " +
                       "items.subjects, " +
                       "items.downloads, " +
                       "ratings.rating "+
                "FROM items LEFT JOIN " +
                "(SELECT * FROM ratings WHERE user_id = ?) AS ratings " +
                "ON items.id = ratings.item_id " +
                "WHERE ? = ANY (items.languages) " +
                "ORDER BY items.date_created DESC");
            select.setLong(1, user_id);
            select.setString(2, language);
            select_result = select.executeQuery();
            while (select_result.next())
            {
                final long item_id =
                    select_result.getLong("item_id");
                final String creator =
                    select_result.getString("creator");
                final String creator_link =
                    select_result.getString("creator_link");
                final String title =
                    select_result.getString("title");
                final String date_created =
                    select_result.getString("date_created");
                final String[] languages = (String[])
                    select_result.getArray("languages")
                                 .getArray();
                final String[] subjects = (String[])
                    select_result.getArray("subjects")
                                 .getArray();
                final Integer downloads = (Integer)
                    select_result.getObject("downloads");
                final Double rating = (Double)
                    select_result.getObject("rating");
                output.addLast(new JSONItem(item_id,
                                            creator,
                                            creator_link,
                                            title,
                                            date_created,
                                            languages,
                                            subjects,
                                            downloads,
                                            rating));
            }
        }
        catch (SQLException e)
        {
            Database.printSQLException(e, Main.err);
            db_failure = true;
        }
        catch (Exception e)
        {
            e.printStackTrace(Main.err);
            db_failure = true;
        }
        finally
        {
            Database.close(select_result);
            Database.close(select);
        }
        if (db_failure)
        {
            return JSONItemListResponse.failure("db", user_id);
        }
        else if (output == null)
        {
            return JSONItemListResponse.failure("db", user_id);
        }
        else
        {
            return JSONItemListResponse.success(user_id, output);
        }
    }

    public final JSONResponse languageList(final Connection db)
    {
        Statement select = null;
        ResultSet select_result = null;
        LinkedList<JSONLanguage> output = new LinkedList<JSONLanguage>();
        boolean db_failure = false;
        try
        {
            // select all items with the user's ratings
            select = db.createStatement();
            select_result = select.executeQuery(
                "SELECT language " +
                "FROM languages " +
                "ORDER BY language");
            while (select_result.next())
            {
                final String language =
                    select_result.getString("language");
                output.addLast(new JSONLanguage(language));
            }
        }
        catch (SQLException e)
        {
            Database.printSQLException(e, Main.err);
            db_failure = true;
        }
        catch (Exception e)
        {
            e.printStackTrace(Main.err);
            db_failure = true;
        }
        finally
        {
            Database.close(select_result);
            Database.close(select);
        }
        if (db_failure)
        {
            return JSONLanguageListResponse.failure("db");
        }
        else if (output == null)
        {
            return JSONLanguageListResponse.failure("db");
        }
        else
        {
            return JSONLanguageListResponse.success(output);
        }
    }

    public final JSONResponse recommendationUpdate(final Connection db,
                                                   final long user_id,
                                                   final long item_id,
                                                   final double rating)
    {
        PreparedStatement upsert = null;
        ResultSet upsert_result = null;
        boolean db_failure = false;
        try
        {
            // update ratings table
            upsert = db.prepareStatement("SELECT rate(?, ?, ?)");
            upsert.setLong(1, user_id);
            upsert.setLong(2, item_id);
            upsert.setDouble(3, rating);
            upsert_result = upsert.executeQuery();
            db.commit();
        }
        catch (SQLException e)
        {
            Database.printSQLException(e, Main.err);
            Database.rollback(db);
            db_failure = true;
        }
        catch (Exception e)
        {
            e.printStackTrace(Main.err);
            Database.rollback(db);
            db_failure = true;
        }
        finally
        {
            Database.close(upsert_result);
            Database.close(upsert);
        }
        if (db_failure)
        {
            return JSONRecommendationUpdateResponse.failure("db", user_id);
        }
        // get recommendations
        List<JSONRecommendation> output = this.recommend(db, user_id);
        if (output == null)
        {
            return JSONRecommendationUpdateResponse.failure("lenskit", user_id);
        }
        else
        {
            return JSONRecommendationUpdateResponse.success(user_id, output);
        }
    }

    public final JSONResponse recommendationList(final Connection db,
                                                 final long user_id)
    {
        List<JSONRecommendation> output = this.recommend(db, user_id);
        if (output == null)
        {
            return JSONRecommendationListResponse.failure("lenskit", user_id);
        }
        else
        {
            return JSONRecommendationListResponse.success(user_id, output);
        }
    }

    private final List<JSONRecommendation> recommend(final Connection db,
                                                     final long user_id)
    {
        try
        {
            // based on http://lenskit.org/documentation/basics/getting-started/
            final LenskitRecommender session = this.recommenderSession(db);
            final ItemRecommender items = session.getItemRecommender();
            List<ScoredId> recommendations = items.recommend(user_id, 10);
            RatingPredictor rating = session.getRatingPredictor();
            LinkedList<JSONRecommendation> output =
                new LinkedList<JSONRecommendation>();

            for (ScoredId recommendation : recommendations)
            {
                final long item_id = recommendation.getId();
                final double rating_expected = rating.predict(user_id,
                                                              item_id);
                output.addLast(new JSONRecommendation(item_id,
                                                      rating_expected));
            }
            return output;
        }
        catch (Exception e)
        {
            e.printStackTrace(Main.err);
            return null;
        }
    }

    private static LenskitConfiguration configuration()
    {
        // based on http://lenskit.org/documentation/basics/getting-started/
        LenskitConfiguration config = new LenskitConfiguration();

        // a basic item-item kNN recommender with baseline

        // Use item-item CF to score items
        // (see http://lenskit.org/documentation/algorithms/item-item/)
        config.bind(ItemScorer.class)
              .to(ItemItemScorer.class);
        // use personalized mean rating as the baseline/fallback predictor.
        // 2-step process:
        // First, use the user mean rating as the baseline scorer
        config.bind(BaselineScorer.class, ItemScorer.class)
              .to(UserMeanItemScorer.class);
        // Second, use the item mean rating as the base for user means
        config.bind(UserMeanBaseline.class, ItemScorer.class)
              .to(ItemMeanRatingItemScorer.class);
        // and normalize ratings by baseline prior to computing similarities
        config.bind(UserVectorNormalizer.class)
              .to(BaselineSubtractingUserVectorNormalizer.class);

        // rating predictor (not ItemScorer) will clamp the rating output
        // to be within [0.5 .. 5] with granularity 0.5
        // (the default is to not clamp)
        config.bind(PreferenceDomain.class)
              .to(new PreferenceDomain(LenskitData.RATING_MIN,
                                       LenskitData.RATING_MAX, 0.5));
        // explicit default configuration values
        config.set(NeighborhoodSize.class).to(20);     // default
        config.set(MinNeighbors.class).to(1);          // default
        config.set(ModelSize.class) // value = 4000 commonly used
              .to(0);                                  // default

        config.set(SimilarityDamping.class).to(0.0);   // default
        config.set(ThresholdValue.class).to(0.0);      // default
        //config.bind(ItemSimilarityThreshold.class)
        //      .to(AbsoluteThreshold.class);            // default
        config.within(ItemSimilarity.class)
              .bind(VectorSimilarity.class)
              .to(CosineVectorSimilarity.class);       // default
        return config;
    }

    private static JDBCRatingDAO ratingsDAO(Connection db)
    {
        JDBCRatingDAOBuilder builder = JDBCRatingDAO.newBuilder();
        // based on org.grouplens.lenskit.data.sql.BasicSQLStatementFactory
        builder.setTableName("ratings");                // default
        builder.setUserColumn("user_id");               // was "user"
        builder.setItemColumn("item_id");               // was "item"
        builder.setRatingColumn("rating");              // default
        builder.setTimestampColumn("timestamp");        // default
        return builder.build(db);
    }

}

