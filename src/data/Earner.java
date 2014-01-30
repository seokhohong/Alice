package data;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import event.PaymentEvents;
import main.Personality;
import sql.Blob;
import sql.SQLDatabase;
import sql.SQLUtils;
import utils.MySerializer;
import utils.StringOps;
import worker.NoteWriter;

public class Earner extends Artist implements Comparable<Earner>
{
	
	static final String COL_NAME = "COL_NAME";
	private static final String COL_WATCHES = "COL_WATCHES";
	private static final String COL_LLAMAS = "COL_LLAMAS";
	private static final String COL_FAVS = "COL_FAVS";
	private static final String COL_POINTS = "COL_POINTS";
	private static final String COL_ENROLLMENTS = "COL_ENROLLMENTS";
	private static final String COL_COMMENTS = "COL_COMMENTS";
	private static final String COL_BANNED = "COL_BANNED";
	private static final String COL_WATCH_HIST = "COL_WATCH_HIST";
	private static final String COL_LLAMA_HIST = "COL_LLAMA_HIST";
	private static final String COL_FAV_HIST = "COL_FAV_HIST";
	private static final String COL_COMMENT_HIST = "COL_COMMENT_HIST";
	private static final String COL_CRITIQUE_HIST = "COL_CRITIQUE_HIST";
	private static final String COL_ENROLL_HIST = "COL_ENROLL_HIST";
	private static final String COL_UNWATCH = "COL_UNWATCH";
	private static final String COL_UNENROLL = "COL_UNENROLL";
	
	private static final String KARMA_TAG = "karma";				public static String getKarmaTag() { return KARMA_TAG; }
	
	private static final int MAX_FAVS = 5;	/* Per payer*/			public static int getMaxFavs() { return MAX_FAVS; }
	
	private static final Personality alice = new Personality();

	private Map<String, Integer> favoriteHistory = new HashMap<String, Integer>();
	private Set<String> llamaHistory = new HashSet<String>();
	private Set<String> watchHistory = new HashSet<String>();
	private Set<String> enrollmentHistory = new HashSet<String>();
	private Set<Integer> critiqueHistory = new HashSet<Integer>(); 	//artwork IDs that have been critiqued
	private Set<Integer> commentHistory = new HashSet<Integer>();
	
	//Counts the number for replying correctly
	private Set<Critique> critiquesReceived = new HashSet<Critique>();
	
	private boolean banned = false;					boolean isBanned() { return banned; }
	
	private boolean activityHidden = false;			public synchronized void activityHidden() { activityHidden = true; }
													public synchronized void activityFound() { activityHidden = false; }
													
	@Deprecated
	private long lastCreditedJournal = 0L;
	
	private int watches = 0;
	private int favs = 0;
	private int llamas = 0;
	private int enrollments = 0;
	
	@Deprecated
	private static final double KARMA_PER_POINT = 1d;
	@Deprecated
	private static final int KARMA_BONUS = 2;
	@Deprecated
	private boolean acceptsComments = false;
	@Deprecated
	private boolean receivedFreeBonus = false;
	@Deprecated
	private double karma = 0;
	private boolean attemptedComments = false;							public void attemptedComments() { attemptedComments = true; }
	private int comments = 0;
	private Set<Comment> recentComments = new HashSet<Comment>();
	private int failedComments = 0;
	
	private int unwatch = 0;
	private int unenroll = 0;
	
	private float points = 0;
	
	private SQLDatabase database;
	
	/** Will attempt to load from the database */
	public Earner(SQLDatabase database, String name)
	{
		super(name);
		this.database = database;
		loadDB(database);
	}
	@Deprecated
	public Earner(File earner) 
	{
		super(earner);
		try
		{
			load(earner);
		} catch(Exception e)
		{
			
		}
	}
	private void loadDB(SQLDatabase database)
	{
		synchronized(database)
		{
			ResultSet rs = database.executeQuery("select * from "+EarnerDatabase.TABLE+" where "+COL_NAME+"=\""+name+"\"");
			try
			{
				if(rs.next()) //only one entry expected
				{
					watches = rs.getInt(COL_WATCHES);
					llamas = rs.getInt(COL_LLAMAS);
					favs = rs.getInt(COL_FAVS);
					enrollments = rs.getInt(COL_ENROLLMENTS);
					comments = rs.getInt(COL_COMMENTS);
					//Remove cast after legacy data is gone
					points = (float) rs.getDouble(COL_POINTS);
					banned = rs.getInt(COL_BANNED) == 1;
					watchHistory = Blob.decompress(rs.getString(COL_WATCH_HIST));
					llamaHistory = Blob.decompress(rs.getString(COL_LLAMA_HIST));
					favoriteHistory = Blob.decompressStringIntMap(rs.getString(COL_FAV_HIST));
					commentHistory = Blob.decompressIntSet(rs.getString(COL_COMMENT_HIST));
					critiqueHistory = Blob.decompressIntSet(rs.getString(COL_CRITIQUE_HIST));
					enrollmentHistory = Blob.decompress(rs.getString(COL_ENROLL_HIST));
					unwatch = rs.getInt(COL_UNWATCH);
					unenroll = rs.getInt(COL_UNENROLL);
				}
			}
			catch(SQLException e)
			{
				e.printStackTrace();
			}
		}
	}
	private synchronized void resetActivity()
	{
		watches = 0;
		favs = 0;
		llamas = 0;
		comments = 0;
		enrollments = 0;
		attemptedComments = false;
		recentComments.clear();
		critiquesReceived.clear();
	}
	public synchronized float getPoints()
	{
		return points;
	}
	@Deprecated
	/** Debugging purposes ONLY*/
	public synchronized void setPoints(double d)
	{
		points = (float) d;
	}
	@Deprecated
	public synchronized void addPoints(double d)
	{
		points += d;
		save();
	}
	private synchronized boolean hasWholePoints()
	{
		return points >= 1;
	}
	/** Leaves only the unpayable fraction of this earner's points, unless there is no donation widget */
	private synchronized void subtractPoints()
	{
		//calculate points
		if(!cannotFindDonationWidget)
		{
			int fund = (int) points; //remove all whole points
			if(fund > 0)
			{
				points = points - fund;
				System.out.println("Removing "+fund+" Points from "+name);
			}
			resetActivity();
		}
	}
	
	public synchronized void redeem(PaymentEvents paymentEvents)
	{
		paymentEvents.addPayment(this, (int) (karma / KARMA_PER_POINT), "You've reedemed "+karma+" Karma!");
	}
	
	synchronized void acceptTransaction(Transaction transaction, NoteWriter noteWriter)
	{
		double karmaNum = Transaction.applyBonuses(transaction.getAmount()) * KARMA_PER_POINT;
		System.out.println("Adding "+karmaNum+" karma to "+transaction.getOrigin());
		karma += karmaNum;
		noteWriter.send(name, "Donation Received!", thankYouMessage());
		save();
	}
	private String thankYouMessage()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Thank you for donating! :heart: Your current Karma balance is ");
		builder.append(karma);
		builder.append(".\n\n");
		builder.append(" - automated by Program Alice");
		return builder.toString();
	}
	/** Earner has done a critique */
	public synchronized boolean submitCritique(Critique critique)
	{
		if(!hasCritiqued(critique.getArtworkId()))
		{
			critiqueHistory.add(critique.getArtworkId());
			critiquesReceived.add(critique);	//so replies correctly detail the number received
			return true;
		}
		return false;
	}
	public synchronized boolean hasCritiqued(int artworkId)
	{
		return critiqueHistory.contains(artworkId);
	}

	//Returns whether this earner accepts the fav
	public synchronized boolean addFavFrom(Payer payer)
	{
		if(!canEarn) return false;
		if(payer.accepts(Currency.FAV))
		{
			String payerName = payer.getName();
			if(numFavsOn(payerName) < MAX_FAVS)
			{
				favoriteHistory.put(payerName, numFavsOn(payerName) + 1);
				registerFav();
				return true;
			}
		}
		return false;
	}
	private synchronized void registerFav()
	{
		favs++;
		points += Currency.FAV.getPayout();
		System.out.println("Successfully Added Fav! Earner "+name+" has "+favs+" favs total");
	}
	public synchronized int numFavsOn(String payerName)
	{
		return favoriteHistory.containsKey(payerName) ? favoriteHistory.get(payerName) : 0;
	}
	public synchronized boolean addWatchFrom(Payer payer)
	{
		if(!canEarn) return false;
		if(payer.accepts(Currency.WATCH))
		{
			String payerName = payer.getName();
			if(!watches(payerName))
			{
				watches++;
				points += Currency.WATCH.getPayout();
				System.out.println("Successfully Added Watch! Earner "+name+" has "+watches+" watches total");
				watchHistory.add(payerName);
				return true;
			}
		}
		return false;
	}
	public synchronized boolean addWatchFrom(Group group)
	{
		if(!canEarn) return false;
		if(group.accepts(Currency.WATCH))
		{
			String groupName = group.getName();
			if(!watches(groupName))
			{
				watches++;
				points += Currency.WATCH.getPayout();
				System.out.println("Successfully Added Watch! Earner "+name+" has "+watches+" watches total");
				watchHistory.add(groupName);
				return true;
			}
		}
		return false;
	}
	public synchronized boolean watches(String payerName)
	{
		return watchHistory.contains(payerName);
	}

	public synchronized boolean addLlamaFrom(Payer payer)
	{
		if(!canEarn) return false;
		if(payer.accepts(Currency.LLAMA))
		{
			String payerName = payer.getName();
			if(!llamaed(payerName))
			{
				llamas++;
				points += Currency.LLAMA.getPayout();
				System.out.println("Successfully Added Llama! Earner "+name+" has given "+llamas+" llamas total");
				llamaHistory.add(payerName);
				return true;
			}
		}
		return false;
	}
	public synchronized boolean llamaed(String payerName)
	{
		return llamaHistory.contains(payerName);
	}
	public synchronized boolean addEnrollmentFrom(Group group)
	{
		if(!canEarn) return false;
		if(group.accepts(Currency.ENROLLMENT))
		{
			String groupName = group.getName();
			if(!enrolled(groupName))
			{
				enrollments++;
				points += Currency.ENROLLMENT.getPayout();
				System.out.println("Successfully Added Enrollment! Earner "+name+" has given "+enrollments+" enrollments total");
				enrollmentHistory.add(groupName);
				return true;
			}
		}
		return false;
	}
	public synchronized boolean enrolled(String groupName)
	{
		return enrollmentHistory.contains(groupName);
	}
	public synchronized boolean accepts(Currency currency)
	{
		return currency == Currency.COMMENT && acceptsComments;
	}
	public synchronized void setPreferences(Currency currency, boolean accepts)
	{
		if(currency == Currency.COMMENT)
		{
			if(karma >= Currency.COMMENT.getCost())
			{
				acceptsComments = accepts;
				if(accepts && !receivedFreeBonus)
				{
					receivedFreeBonus = true;
					karma += KARMA_BONUS;
				}
			}
		}
		save();
	}
	/** Returns whether the comment was added with success */
	public synchronized boolean addComment(Comment comment, CommentsHistory commentsHistory)
	{
		if(!canEarn) return false;
		if(!commentHistory.contains(comment.getArtworkId()))
		{
			commentsHistory.addComment(comment);
			comments++;
			points += Currency.COMMENT.getPayout();
			//received.receive(Currency.COMMENT);
			System.out.println("Successfully Added Comment! Earner "+name+" has given "+comment+" comments total");
			recentComments.add(comment);
			checkForQuestionableComments(commentsHistory);	//ok, sort of wasteful computationally speaking
			commentHistory.add(comment.getArtworkId());
			return true;
		}
		return false;
	}
	public synchronized void rejectComment(Comment comment)
	{
		failedComments ++;
	}
	/*
	private synchronized void receive(Currency currency)
	{
		if(currency != Currency.COMMENT) return;
		karma -= Currency.COMMENT.getPayout();
		if(karma < Currency.COMMENT.getCost()) //out of karma?
		{
			acceptsComments = false;
		}
	}
	*/
	private static final int LAG = 1000 * 5;
	private static final int TOO_QUICK = 1000 * 10; //time at which delay between comments is unreasonable
	private void checkForQuestionableComments(CommentsHistory commentsHistory)
	{
		for(Comment aComment : recentComments)
		{
			for(Comment bComment : recentComments)
			{
				if(aComment != bComment)
				{
					boolean similar = aComment.tooSimilar(bComment);
					if(aComment.timeDifference(bComment) < LAG && similar) //probably DA lagging here
					{
						continue;
					}
					if(aComment.timeDifference(bComment) < TOO_QUICK)
					{
						commentsHistory.addEvent("Suspiciously fast Commenting between "+aComment+" and "+bComment);
						banned = true;
					}
					if(similar)
					{
						commentsHistory.addEvent("Banning "+name+" for "+aComment+" and "+bComment+" being too similar!");
						banned = true;
					}

				}
			}
		}
	}
	
	/** Checks that people don't go back on what they did earlier, otherwise deducts points */
	public synchronized void markUnwatch(String payer)
	{
		if(watches(payer))
		{
			unwatch++;
			points += Currency.PENALTY;
			watchHistory.remove(payer);
			System.out.println(name+" unwatched "+payer+"!");
			save();
		}
	}
	public synchronized void checkUnenroll(Set<String> allGroups)
	{
		Iterator<String> iter = enrollmentHistory.iterator();
		while(iter.hasNext())
		{
			String group = iter.next();
			if(!allGroups.contains(group))
			{
				unenroll++;
				points += Currency.PENALTY;
				enrollmentHistory.remove(group);
				System.out.println(name+" unenrolled "+group+"!");
				iter.remove();
			}
		}
	}
	
	
	public synchronized String getReplyMessage(String capitalizedName)
	{
		StringBuilder comment = new StringBuilder();
		
		if(banned)
		{
			bannedComment(comment);
			return comment.toString();
		}
		if(!canEarn)
		{
			rejectAccount(comment);
			return comment.toString();
		}
		
		standardComment(comment, capitalizedName);
		
		return comment.toString();
	}
	//Comment builders
	private void rejectAccount(StringBuilder comment)
	{
		comment.append("\nI'm sorry, but your account currently does not meet the requirements for earning points at this account. Please refer to the <a href=\"http://fav.me/d6hbevt\">Earning Points</a> journal for more information.");
	}
	private void bannedComment(StringBuilder comment)
	{
		comment.append("\nYour account has been banned, most likely from the abuse of our Critiques/Comments service. Please send a note if you think this is in error.");
	}
	private void noDonationWidget(StringBuilder comment)
	{
		if(cannotFindDonationWidget)
		{
			comment.append("You don't have a donation widget! :ohnoes: Please make a <b>NEW COMMENT</b> on my profile once you add one to your page, and I'll send you your points!\n\n");
		}
	}
	private void commentNotifications(StringBuilder comment)
	{
		if(activityHidden)
		{
			comment.append("Your activities page is hidden! If you gave comments and want credit for them, please unhide your activities.\n\n");
		}
		//If failedComments isn't 0, then there's an alternate message
		if(attemptedComments && comments == 0 && commentHistory.isEmpty() && failedComments == 0)
		{
			comment.append("You seem to have attempted to give comments, but Alice did not give credit for them. Please refer to the <a href=\"http://fav.me/d6hdkvu\">Comment Guidelines</a>\n\n");
		}
	}
	private void critiqueComment(StringBuilder comment)
	{
		if(!critiquesReceived.isEmpty())
		{
			if(critiquesReceived.size() == 1)
			{
				comment.append("I've received <b>1</b> critique! ");
				comment.append("Once your critique has been marked as fair, you will be sent your points.");
			}
			else
			{
				comment.append("I've received <b>" + critiquesReceived.size() + "</b> critiques! ");
				comment.append("You will be sent your points when the critiques are marked as fair by each Deviant.");	
			}
			comment.append(" If you have any questions, please refer to the <a href=\"http://fav.me/d6hdkqd\">Critique Guidelines</a>.\n\n");
		}
		critiquesReceived.clear(); //so the next comment doesn't double-count in the reply
	}
	//Has this earner done anything here?
	private boolean totallyNewEarner()
	{
		return watchHistory.isEmpty() && llamaHistory.isEmpty() && favoriteHistory.isEmpty() && critiqueHistory.isEmpty() && commentHistory.isEmpty();
	}
	private void noPoints(StringBuilder comment, String capitalizedName)
	{
		if(totallyNewEarner())
		{
			comment.append("Hey, <b>" + capitalizedName + "</b>! :wave: Welcome to dATrade!\n\n");
			comment.append("Want to earn points? Give watches, llamas, favs, or comments to the Featured Deviants at the top of my page. ");
			comment.append("Refer to my <a href=\"http://fav.me/d6hbevt\">Earning Points</a> journal for more information.\n\n");
			comment.append("Want to be listed as a Featured Deviant? Read my <a href=\"http://fav.me/d6hbehb\">Donating Points</a> journal to learn more.");
		}
		else
		{
			comment.append("I'm sorry, but you have not earned any points.\n\n");
		}
	}
	private void pleaseLeaveNewComment(StringBuilder comment)
	{
		comment.append("Please make sure to leave a <b>NEW COMMENT</b> on my profile page, or I will not be able to send you your points! :ohnoes:\n\n");
	}
	private void normalPoints(StringBuilder comment, String capitalizedName) //need to pass it in as parameter
	{
		comment.append(alice.randomGreeting() + "<b>" + capitalizedName + "</b>! " + alice.randomEmote() + "\n\n You've been given credit for " + getImmediateCreditString());
		comment.append(" for a total of <b>"+StringOps.roundDown2(points)+" points</b>! \n\n");
	}
	private void moreCommand(StringBuilder comment)
	{
		comment.append("For an easy way to see what there's still left for you to do, type <b>#more</b> in a new comment on my profile.\n\n");
	}
	private void failedComments(StringBuilder comment)
	{
		if(failedComments > 0)
		{
			comment.append("<b>"+failedComments+"</b> of your recently posted comments failed to meet the Comment Guidelines criteria. Please refer to the <a href=\"http://fav.me/d6hdkvu\">Comment Guidelines</a> journal for more information.\n\n");
		}
		failedComments = 0;
	}
	private void salutation(StringBuilder comment)
	{
		comment.append("- automated by Program Alice");
	}
	private void unwatchedNotification(StringBuilder comment)
	{
		if(unwatch > 0 && unenroll > 0)
		{
			Map<Currency, Integer> undoQuantities = new HashMap<Currency, Integer>();
			undoQuantities.put(Currency.WATCH, unwatch);
			undoQuantities.put(Currency.ENROLLMENT, unenroll);
			System.out.println("Please do not unwatch or unenroll! You have been penalized for taking back "+Currency.assembleList(undoQuantities));
			unwatch = 0;
			unenroll = 0;
		}
	}
	private void standardComment(StringBuilder comment, String capitalizedName)
	{
		if(critiquesReceived.isEmpty())
		{
			if(points == 0)
			{
				noPoints(comment, capitalizedName);
				pleaseLeaveNewComment(comment);
			}
			else if(points < 1)
			{
				tooFewPoints(comment);
			}
		}
		if(points >= 1)
		{
			normalPoints(comment, capitalizedName);//imagine this as f(x, y)
		}
		
		critiqueComment(comment);
		failedComments(comment);
		commentNotifications(comment);
		noDonationWidget(comment);
		moreCommand(comment);
		unwatchedNotification(comment);
		
		salutation(comment);
	}
	private void tooFewPoints(StringBuilder comment)
	{
		if(watches == 0 && llamas == 0 && favs == 0 && comments == 0)
		{
			comment.append("I'm sorry, but you have not earned any points.\n\n");
		}
		else
		{
			comment.append("I'm sorry, but you have not earned any full points. You've been given credit for " + getImmediateCreditString() + " for a total of<b> "+StringOps.roundDown2(points)+" points</b>. \n\n When you earn more points, these partial points will be added to your current balance.\n\n");
		}
	}
	public synchronized void pay(PaymentEvents paymentEvents)
	{
		if(getPoints() != 0)
		{
			if(hasWholePoints() && hasDonationWidget())
			{
				paymentEvents.addPayment(this, (int) getPoints(), getDonateMessage());
			}
			else
			{
				System.out.println(Thread.currentThread()+" Not Paying "+name);
			}
			save(); //save earner the moment payment is enqueued
		}
	}
	private static final int MAX_DONATE_CHARLENGTH = 250;
	private synchronized String getDonateMessage()
	{
		StringBuilder message = new StringBuilder();

		message.append(" You've been given credit for ");
		message.append(getImmediateCreditString());
		message.append(" for a total of "+StringOps.roundDown2(points)+" points!");

		return message.toString().substring(0, Math.min(MAX_DONATE_CHARLENGTH, message.length()));
	}
	//Returns the current credit for the currencies
	private String getImmediateCreditString()
	{
		Map<Currency, Integer> counts = new HashMap<Currency, Integer>();
		counts.put(Currency.WATCH, watches);
		counts.put(Currency.LLAMA, llamas);
		counts.put(Currency.FAV, favs);
		counts.put(Currency.COMMENT, comments);
		counts.put(Currency.ENROLLMENT, enrollments);
		return Currency.assembleList(counts);
	}
	public synchronized String getStats()
	{
		StringBuilder message = new StringBuilder();
		message.append("You've given:\n");
		Map<Currency, Integer> counts = new HashMap<Currency, Integer>();
		counts.put(Currency.WATCH, watchHistory.size());
		counts.put(Currency.LLAMA, llamaHistory.size());
		counts.put(Currency.FAV, totalFavsGiven());
		counts.put(Currency.COMMENT, commentHistory.size());
		counts.put(Currency.CRITIQUE, critiqueHistory.size());
		message.append(Currency.assembleList(counts));
		message.append("\n\n");
		return message.toString();
	}
	private int totalFavsGiven()
	{
		int countFavs = 0;
		for(int given : favoriteHistory.values())
		{
			countFavs += given;
		}
		return countFavs;
	}
	
	@Override
	public int compareTo(Earner otherEarner) 
	{
		if(otherEarner.karma > karma)
		{
			return 1;
		}
		else if(otherEarner.karma < karma)
		{
			return -1;
		}
		return 0;
	}
	
	public static void makeTable(SQLDatabase database)
	{
		database.executeUpdate("create table earners("
			+ COL_NAME + " varchar(64),"
			+ COL_WATCHES + " int,"
			+ COL_LLAMAS + " int,"
			+ COL_FAVS + " int,"
			+ COL_ENROLLMENTS + " int,"
			+ COL_COMMENTS + " int,"
			+ COL_POINTS + " double,"
			+ COL_BANNED + " int," //boolean (0, 1)
			+ COL_WATCH_HIST + " mediumblob,"
			+ COL_LLAMA_HIST + " mediumblob,"
			+ COL_FAV_HIST + " mediumblob,"
			+ COL_COMMENT_HIST + " mediumblob,"
			+ COL_CRITIQUE_HIST + " mediumblob,"
			+ COL_ENROLL_HIST + " mediumblob,"
			+ COL_UNENROLL + " int,"
			+ COL_UNWATCH + " int);");
	}
	/** Fix the gimmicky subtractPoints usage */
	public void save()
	{
		synchronized(database)
		{
			subtractPoints();
			database.executeUpdate("delete from "+EarnerDatabase.TABLE+" where "+COL_NAME+" = "+SQLUtils.format(name));
			database.executeUpdate(getSQLSave());
		}
	}
	
	public String getSQLSave()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("insert into "+EarnerDatabase.TABLE+" values("
				+ SQLUtils.format(name) +","
				+ watches + ","
				+ llamas + ","
				+ favs + ","
				+ enrollments + ","
				+ comments + ","
				+ points + ","
				+ (banned ? 1 : 0) + ","
				+ SQLUtils.format(Blob.compress(watchHistory)) + ","
				+ SQLUtils.format(Blob.compress(llamaHistory)) + ","
				+ SQLUtils.format(Blob.compress(favoriteHistory)) + ","
				+ SQLUtils.format(Blob.compress(commentHistory)) + ","
				+ SQLUtils.format(Blob.compress(critiqueHistory)) + ","
				+ SQLUtils.format(Blob.compress(enrollmentHistory)) + ","
				+ unwatch + ","
				+ unenroll + ");");
		return builder.toString();
	}
	
	public synchronized void load(File f) throws IOException, ParseException
	{
		BufferedReader buff = new BufferedReader(new FileReader(f));
		try
		{
			MySerializer.readMapStringInteger(buff, favoriteHistory);
			MySerializer.read(buff, watchHistory);
			MySerializer.read(buff, llamaHistory);
			lastCreditedJournal = Long.parseLong(MySerializer.read(buff));
			name = MySerializer.read(buff);
			watches = Integer.parseInt(MySerializer.read(buff));
			llamas = Integer.parseInt(MySerializer.read(buff));
			favs = Integer.parseInt(MySerializer.read(buff));
			points = Float.parseFloat(MySerializer.read(buff));
			banned = Boolean.parseBoolean(MySerializer.read(buff));
			MySerializer.readSetInteger(buff, critiqueHistory);
			String line = MySerializer.read(buff);
			if(line == null)
			{
				return;
			}
			comments = Integer.parseInt(line);
			MySerializer.readSetInteger(buff, commentHistory);
			enrollments = Integer.parseInt(MySerializer.read(buff));
			MySerializer.read(buff, enrollmentHistory);
			karma = Double.parseDouble(MySerializer.read(buff));
			acceptsComments = Boolean.parseBoolean(MySerializer.read(buff));
			receivedFreeBonus = Boolean.parseBoolean(MySerializer.read(buff));
			unwatch = Integer.parseInt(MySerializer.read(buff));
			unenroll = Integer.parseInt(MySerializer.read(buff));
		}
		catch(NumberFormatException | NullPointerException e)
		{
			e.printStackTrace();
			System.err.println("Could not read everything, defaulting some values");
			save();
		}
		buff.close();
	}
	@Override
	public String toString()
	{
		return name;
	}
}
