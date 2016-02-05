/*
 * @Author Wei-Tsung Lin
 * @Date 02/04/2016
 * @Description Update all prices in datastore
 */
package cs263w16;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import cs263w16.amazon.JavaCodeSnippet;

@SuppressWarnings("serial")
public class UpdateServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		// System.out.println((new Date()).toString() + "Cron job triggered.");
		resp.setContentType("text/html");
		resp.getWriter().println("<html><body>");

		DatastoreService datastore = DatastoreServiceFactory
				.getDatastoreService();
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		JavaCodeSnippet jcs = new JavaCodeSnippet();

		Query query = new Query("WishlistProduct");
		query.addSort(Entity.KEY_RESERVED_PROPERTY, SortDirection.ASCENDING);
		List<Entity> results = datastore.prepare(query).asList(
				FetchOptions.Builder.withDefaults());
		// List all entities
		for (Entity entity : results) {
			String productID = entity.getKey().getName();
			resp.getWriter().println(
					"<b>" + productID + ":</b>	"
							+ entity.getProperty("productName") + "<br>");
			// Check if the current price is lower
			try {
				WishlistProduct wishlistProduct = jcs.search(productID);
				double price = wishlistProduct.getCurrentPrice();
				if (price < (double) entity.getProperty("lowestPrice")) {
					entity.setProperty("lowestPrice", price);
					entity.setProperty("lowestDate", new Date());
					datastore.put(entity);
					syncCache.put(productID, entity);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		resp.getWriter().println("</body></html>");
	}
}