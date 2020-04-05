//-*-Mode:java;coding:utf-8;tab-width:4;c-basic-offset:4;indent-tabs-mode:()-*-
// ex: set ft=java fenc=utf-8 sts=4 ts=4 sw=4 et nomod:

package org.cloudi.examples.tutorial;

import java.util.List;
import java.util.LinkedList;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.mahout.common.MemoryUtil;
import org.apache.mahout.cf.taste.common.NoSuchUserException;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.eval.LoadEvaluator;
import org.apache.mahout.cf.taste.impl.model.jdbc.ReloadFromJDBCDataModel;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingItemSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

public class RecommendData
{
    public static final double RATING_MIN = 0.5;
    public static final double RATING_MAX = 5.0;
    public static final double RATING_STEP = 0.5;

    private final DataSource db_data;
    private final Recommender recommender;

    public RecommendData(final DataSource db_data)
        throws TasteException
    {
        // a basic item-based recommender (Collaborative Filtering)
        final ReloadFromJDBCDataModel model =
            new ReloadFromJDBCDataModel(Database.dataModel(db_data));
        final ItemSimilarity similarity = new CachingItemSimilarity(
            new PearsonCorrelationSimilarity(model), model);
        this.db_data = db_data;
        this.recommender = new CachingRecommender(
            new GenericItemBasedRecommender(model, similarity));
        LoadEvaluator.runLoad(this.recommender);
    }

    public final DataSource dataSource()
    {
        return this.db_data;
    }

    public void shutdown()
    {
        MemoryUtil.stopMemoryLogger();
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
        }
        // Apache Mahout has thread pools that do not stop
        // (stuck on LinkedBlockingQueue)
        System.exit(1);
    }

    public final JSONResponse itemList(final long user_id,
                                       final String language,
                                       final String subject)
    {
        Connection db = null;
        PreparedStatement select = null;
        ResultSet select_result = null;
        boolean db_failure = false;
        LinkedList<JSONItem> output = new LinkedList<JSONItem>();
        try
        {
            // select all items with the user's ratings
            db = Database.connection(this.db_data);
            String subject_query;
            if (subject != null)
            {
                subject_query = "AND ? = ANY (items.subjects) ";
            }
            else
            {
                subject_query = "";
            }
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
                "WHERE " +
                "? = ANY (items.languages) " +
                subject_query +
                "ORDER BY items.date_created DESC, items.title ASC");
            select.setLong(1, user_id);
            select.setString(2, language);
            if (subject != null)
                select.setString(3, subject);
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
            Database.close(db);
        }
        if (db_failure)
        {
            return JSONItemListResponse.failure("db", user_id,
                                                language, subject);
        }
        else
        {
            return JSONItemListResponse.success(user_id,
                                                language, subject, output);
        }
    }

    public final JSONResponse languageList()
    {
        Connection db = null;
        Statement select = null;
        ResultSet select_result = null;
        boolean db_failure = false;
        LinkedList<JSONLanguage> output = new LinkedList<JSONLanguage>();
        try
        {
            // select all items with the user's ratings
            db = Database.connection(this.db_data);
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
            Database.close(db);
        }
        if (db_failure)
        {
            return JSONLanguageListResponse.failure("db");
        }
        else
        {
            return JSONLanguageListResponse.success(output);
        }
    }

    public final JSONResponse subjectList()
    {
        Connection db = null;
        Statement select = null;
        ResultSet select_result = null;
        boolean db_failure = false;
        LinkedList<JSONSubject> output = new LinkedList<JSONSubject>();
        try
        {
            // select all items with the user's ratings
            db = Database.connection(this.db_data);
            select = db.createStatement();
            select_result = select.executeQuery(
                "SELECT subject " +
                "FROM subjects " +
                "ORDER BY subject");
            while (select_result.next())
            {
                final String subject =
                    select_result.getString("subject");
                output.addLast(new JSONSubject(subject));
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
            Database.close(db);
        }
        if (db_failure)
        {
            return JSONSubjectListResponse.failure("db");
        }
        else
        {
            return JSONSubjectListResponse.success(output);
        }
    }

    public final JSONResponse recommendationUpdate(final long user_id,
                                                   final long item_id,
                                                   final double rating)
    {
        Connection db = null;
        PreparedStatement upsert = null;
        ResultSet upsert_result = null;
        boolean db_failure = false;
        try
        {
            // update ratings table
            db = Database.connection(this.db_data);
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
            Database.close(db);
        }
        if (db_failure)
        {
            return JSONRecommendationUpdateResponse.failure("db", user_id);
        }
        // get recommendations
        List<JSONRecommendation> output = this.recommend(user_id);
        if (output == null)
        {
            return JSONRecommendationUpdateResponse.failure("recommender",
                                                            user_id);
        }
        else
        {
            return JSONRecommendationUpdateResponse.success(user_id, output);
        }
    }

    public final JSONResponse recommendationList(final long user_id)
    {
        List<JSONRecommendation> output = this.recommend(user_id);
        if (output == null)
        {
            return JSONRecommendationListResponse.failure("recommender",
                                                          user_id);
        }
        else
        {
            return JSONRecommendationListResponse.success(user_id, output);
        }
    }

    private final List<JSONRecommendation> recommend(final long user_id)
    {
        Connection db = null;
        PreparedStatement select = null;
        ResultSet select_result = null;
        boolean recommender_failure = false;
        LinkedList<JSONRecommendation> output =
            new LinkedList<JSONRecommendation>();
        try
        {
            db = Database.connection(this.db_data);
            List<RecommendedItem> recommendations =
                this.recommender.recommend(user_id, 10);
            StringBuffer query = new StringBuffer();
            query.append(
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
                "WHERE false ");
            for (RecommendedItem recommendation : recommendations)
            {
                query.append("OR items.id = '");
                query.append(Long.toString(recommendation.getItemID()));
                query.append("' ");
            }
            query.append(
                "ORDER BY items.date_created DESC, items.title ASC");
            select = db.prepareStatement(query.toString());
            select.setLong(1, user_id);
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
                double rating_expected =
                    this.recommender.estimatePreference(user_id, item_id);
                rating_expected = Math.round(rating_expected /
                                             RecommendData.RATING_STEP) *
                                  RecommendData.RATING_STEP;
                output.addLast(new JSONRecommendation(item_id,
                                                      creator,
                                                      creator_link,
                                                      title,
                                                      date_created,
                                                      languages,
                                                      subjects,
                                                      downloads,
                                                      rating,
                                                      rating_expected));
            }
        }
        catch (NoSuchUserException e)
        {
        }
        catch (SQLException e)
        {
            Database.printSQLException(e, Main.err);
            recommender_failure = true;
        }
        catch (Exception e)
        {
            e.printStackTrace(Main.err);
            recommender_failure = true;
        }
        finally
        {
            Database.close(select_result);
            Database.close(select);
            Database.close(db);
        }
        if (recommender_failure)
        {
            return null;
        }
        else
        {
            return output;
        }
    }
}

