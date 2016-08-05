//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et:

package org.cloudi.examples.tutorial;

import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.ItemRecommender;
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
//import org.grouplens.lenskit.knn.item.MinCommonUsers;
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
    private final LenskitRecommenderEngine engine;

    public LenskitData(Connection db)
        throws RecommenderBuildException
    {
        // based on http://lenskit.org/documentation/basics/data-access/
        LenskitConfiguration config = LenskitData.configuration();
        JDBCRatingDAO dao = LenskitData.ratingsDAO(db);
        LenskitConfiguration dataConfig = new LenskitConfiguration();
        dataConfig.addComponent(dao);
        this.engine =
            LenskitRecommenderEngine.newBuilder()
                                    .addConfiguration(config)
                                    .addConfiguration(dataConfig,
                                                      ModelDisposition.EXCLUDED)
                                    .build();
    }

    private LenskitRecommender recommenderSession(Connection db)
        throws RecommenderConfigurationException
    {
        // based on http://lenskit.org/documentation/basics/data-access/
        JDBCRatingDAO dao = LenskitData.ratingsDAO(db);
        LenskitConfiguration dataConfig = new LenskitConfiguration();
        dataConfig.addComponent(dao);
        return engine.createRecommender(dataConfig);
    }

    public void rate(Connection db,
                     final long user_id,
                     final long item_id,
                     final double rating)
    {
        PreparedStatement upsert = null;
        ResultSet upsertResult = null;
        try
        {
            // update ratings table
            upsert = db.prepareStatement("SELECT rate(?, ?, ?)");
            upsert.setLong(1, user_id);
            upsert.setLong(2, item_id);
            upsert.setDouble(3, rating);
            upsertResult = upsert.executeQuery();
            db.commit();
    
            // based on http://lenskit.org/documentation/basics/getting-started/
            final LenskitRecommender session = this.recommenderSession(db);
            final ItemRecommender items = session.getItemRecommender();
            List<ScoredId> recommendations = items.recommend(user_id, 10);
    
            // XXX add response return value
            Main.info(this, "user(%d)\n", user_id);
            for (ScoredId recommendation : recommendations)
            {
                Main.info(this, "recommendation(%d, %f)\n",
                          recommendation.getId(),
                          recommendation.getScore());
            }
        }
        catch (SQLException e)
        {
            Database.printSQLException(e, Main.err);
            Database.rollback(db);
        }
        catch (Exception e)
        {
            e.printStackTrace(Main.err);
            Database.rollback(db);
        }
        finally
        {
            Database.close(upsertResult);
            Database.close(upsert);
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
              .to(new PreferenceDomain(0.5, 5, 0.5));
        // explicit default configuration values
        config.set(NeighborhoodSize.class).to(20);     // default
        config.set(MinNeighbors.class).to(1);          // default
        config.set(ModelSize.class).to(0);             // default
        //config.set(MinCommonUsers.class).to(0);        // default
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

