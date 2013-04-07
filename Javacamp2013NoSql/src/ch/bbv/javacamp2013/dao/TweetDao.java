package ch.bbv.javacamp2013.dao;

import java.util.Arrays;
import java.util.Date;

import me.prettyprint.cassandra.model.BasicColumnDefinition;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.service.ColumnSliceIterator;
import me.prettyprint.cassandra.service.template.ColumnFamilyTemplate;
import me.prettyprint.cassandra.service.template.ColumnFamilyUpdater;
import me.prettyprint.cassandra.service.template.ThriftColumnFamilyTemplate;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.ddl.ColumnDefinition;
import me.prettyprint.hector.api.ddl.ColumnFamilyDefinition;
import me.prettyprint.hector.api.ddl.ColumnIndexType;
import me.prettyprint.hector.api.ddl.ComparatorType;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.SliceQuery;

/**
 * Implements high level methods to access the "Tweets" column family (table).
 * To get an instance of this object call {@link JavacampKeyspace#getTweetDao}.<br>
 * The table has the following layout:
 * <ul>
 * <li>The key for the row is the tweetid.</li>
 * <li>The col <code>user_id</code> contains the id of the user, that created
 * the tweet.</li>
 * <li>The col <code>body</code> contains the message of the tweet.</li>
 * <li>The col <code>created_at</code> contains the time when the tweet was
 * created.</li>
 * </ul>
 */
public class TweetDao
{
   private static final String COLUMNFAMILY_NAME = "Tweets";

   private static final String COL_TWEET_ID = "id";

   private static final String COL_USER_ID = "user_id";

   private static final String COL_BODY = "body";

   private static final String COL_CREATED_AT = "created_at";

   private final Keyspace _keyspace;

   private final ColumnFamilyTemplate<Long, String> _template;

   TweetDao(Keyspace keyspace)
   {
      _keyspace = keyspace;

      _template = new ThriftColumnFamilyTemplate<Long, String>(_keyspace, // keyspace
            COLUMNFAMILY_NAME, // columnFamily
            LongSerializer.get(), // keySerializer
            StringSerializer.get()); // topSerializer
   }

   /**
    * Adds an tweet to the column family (table)
    * 
    * @param tweetid The id of the tweet.
    * @param userid The id of the user, that created the tweet.
    * @param body The message of the tweet.
    * @param createdAt The time when the tweet was created.
    */
   public void addTweet(long tweetid, long userid, String body, Date createdAt)
   {
      ColumnFamilyUpdater<Long, String> updater = _template.createUpdater(tweetid);
      updater.setLong(COL_TWEET_ID, tweetid);
      updater.setLong(COL_USER_ID, userid);
      updater.setString(COL_BODY, body);
      updater.setDate(COL_CREATED_AT, createdAt);

      try
      {
         _template.update(updater);
      }
      catch (HectorException e)
      {
         e.printStackTrace();
      }
   }

   static ColumnFamilyDefinition getColumnFamilyDefinition(String keyspacename)
   {
      BasicColumnDefinition idColDef = new BasicColumnDefinition();
      idColDef.setName(StringSerializer.get().toByteBuffer(COL_TWEET_ID));
      idColDef.setIndexName(COL_TWEET_ID + "_idx");
      idColDef.setIndexType(ColumnIndexType.KEYS);
      idColDef.setValidationClass(ComparatorType.LONGTYPE.getClassName());

      BasicColumnDefinition userColDef = new BasicColumnDefinition();
      userColDef.setName(StringSerializer.get().toByteBuffer(COL_USER_ID));
      userColDef.setValidationClass(ComparatorType.LONGTYPE.getClassName());

      BasicColumnDefinition bodyColDef = new BasicColumnDefinition();
      bodyColDef.setName(StringSerializer.get().toByteBuffer(COL_BODY));
      bodyColDef.setValidationClass(ComparatorType.UTF8TYPE.getClassName());

      BasicColumnDefinition createdAtColDef = new BasicColumnDefinition();
      createdAtColDef.setName(StringSerializer.get().toByteBuffer(COL_CREATED_AT));
      createdAtColDef.setValidationClass(ComparatorType.DATETYPE.getClassName());

      return HFactory.createColumnFamilyDefinition( // nl
            keyspacename, // keinyspace
            COLUMNFAMILY_NAME, // cfName
            ComparatorType.UTF8TYPE,// comparatorType
            Arrays.asList((ColumnDefinition) idColDef, (ColumnDefinition) userColDef, (ColumnDefinition) bodyColDef,
                  (ColumnDefinition) createdAtColDef));

   }

   private void getTweet(long tweetid)
   {
      SliceQuery<Long, String, String> query = HFactory.createSliceQuery(_keyspace, LongSerializer.get(),
            StringSerializer.get(), StringSerializer.get());

      query.setKey(tweetid);
      query.setColumnFamily(COLUMNFAMILY_NAME);

      ColumnSliceIterator<Long, String, String> iterator = new ColumnSliceIterator<Long, String, String>(query, null,
            "\uFFFF", false);

      while (iterator.hasNext())
      {
         HColumn<String, String> col = iterator.next();
         System.out.println(col.getName() + "=\"" + col.getValue() + "\"");
      }
   }

   public static void main(String[] args)
   {
      TweetDao tweetAccess = new JavacampKeyspace("Test Cluster", "192.168.56.101:9160").getTweetDao();
      long id = 1234;
      tweetAccess.addTweet(id, 23456, "My body", new Date());
      tweetAccess.getTweet(id);
      tweetAccess.getTweet(id);
   }
}