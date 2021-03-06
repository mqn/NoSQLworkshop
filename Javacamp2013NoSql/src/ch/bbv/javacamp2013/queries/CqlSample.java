package ch.bbv.javacamp2013.queries;

import java.io.IOException;

import me.prettyprint.cassandra.model.CqlQuery;
import me.prettyprint.cassandra.model.CqlRows;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.QueryResult;
import ch.bbv.javacamp2013.Config;

/**
 * A simple CQL sample.
 */
public final class CqlSample {

   private CqlSample() {
   }

   /**
    * Starts the program.
    * 
    * @param args Command line arguments
    * @throws IOException If the configuration could not be read.
    */
   public static void main(final String[] args) throws IOException {
      final Config cfg = new Config();
      final Cluster cluster = HFactory.getOrCreateCluster(cfg.getClusterName(), cfg.getClusterAddress());

      final Keyspace systemKeyspace = HFactory.createKeyspace("system", cluster);
      final CqlQuery<String, String, String> cqlQuery = new CqlQuery<>(systemKeyspace, StringSerializer.get(),
            StringSerializer.get(), StringSerializer.get());

      cqlQuery.setQuery("select * from schema_keyspaces");
      // cqlQuery.setQuery("select * from local");
      final QueryResult<CqlRows<String, String, String>> result = cqlQuery.execute();

      for (Row<String, String, String> row : result.get()) {
         System.out.println(row);
      }
   }
}
