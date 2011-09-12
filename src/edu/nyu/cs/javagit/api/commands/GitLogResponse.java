/*
 * ====================================================================
 * Copyright (c) 2008 JavaGit Project.  All rights reserved.
 *
 * This software is licensed using the GNU LGPL v2.1 license.  A copy
 * of the license is included with the distribution of this source
 * code in the LICENSE.txt file.  The text of the license can also
 * be obtained at:
 *
 *   http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 *
 * For more information on the JavaGit project, see:
 *
 *   http://www.javagit.com
 * ====================================================================
 */
package edu.nyu.cs.javagit.api.commands;

import java.util.ArrayList;
import java.util.List;

import edu.nyu.cs.javagit.utilities.CheckUtilities;
import experiments.scm.ScmEntryChangeKind;

/**
 * 	A response data object for the git log command.
 */
public class GitLogResponse implements CommandResponse {
 
	private List<Commit> commitList = new ArrayList<Commit>();
	protected List<ResponseString> errors = new ArrayList<ResponseString>();
	
	//local variables used to store parsed data until it is pushed into the object
	private String sha = null;
	private String parentSha = null;
	private List<String> mergeDetails = null;
	private String message = null;
	private List<CommitFile> files = null;
	private String[] tags = null;
	
	private String author = null;
	private String dateString = null;
	
	private String committer = null;
	private String commitDate = null;
	private String mergeOrigin = null;
	
	/**
	 * 
	 * @return true is the response object contain an error
	 */
	public boolean containsError() {
		return ( errors.size() > 0 );
	}
	
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	
	/**
	 * 
	 * @param sha This sets the sha extracted from each commit entry.
	 */
	public void setSha(String sha){
		this.sha = sha;
	}
	
	/**
	 * 
	 * @param sha This sets the parent commit's sha extracted from each commit entry.
	 */
	public void setParentSha(String sha){
		this.parentSha = sha;
	}
	
	/**
	 * Adds the given parent of the commit. The first parent is always set on
	 * parentSha. If this is set, the next parent(s) will be added to the
	 * mergeDetails.
	 * @param parent to add
	 */
	public void addParentSha(String parentSha) {
		if (this.parentSha == null) {
			this.parentSha = parentSha;
		} else {
			if (this.mergeDetails == null) {
				this.mergeDetails = new ArrayList<String>(2);
				this.mergeDetails.add(this.parentSha); //the 'first' parent is always a mergeParent
			}
			this.mergeDetails.add(parentSha);
		}
	}

	/**
	 * 
	 * @param mergeDetails This stored the merge details of a commit. eg. Merge: f859e80... 55a5e32...
	 */
	public void setMergeDetails(List<String> mergeDetails){
		this.mergeDetails = mergeDetails;
	}
	
	public boolean hasMergeDetails() {
		return this.mergeDetails != null;
	}
	

	public void setMergeOrigin(String fromCommit) {
		this.mergeOrigin  = fromCommit;
	}
	

	/**
	 * 
	 * @param string This returns the Date object for a particular commit.
	 */
	public void setDate(String string){
		this.dateString = string;
	}
	/**
	 * 
	 * @param author This sets the author for a commit.
	 */
	public void setAuthor(String author){
		this.author = author;
	}
	
	public void setCommitDate(String commitDate) {
		this.commitDate = commitDate;
	}
	
	public void setCommitter(String committer) {
		this.committer = committer;
	}
	
	/**
	 * 
	 * @param message This set the message for a commmit.
	 */
	public void setMessage(String message){
		if (this.message == null){
			this.message = message;
		}
		else{
			this.message += "\n" + message;
		}
		
	}
	/**
	 * 
	 * @param file This adds a file entry to the list of files modified for a single commit.
	 */
	public void addFile(CommitFile file){
		if (files==null){
			files = new ArrayList<CommitFile>();
		}
		this.files.add(file);
	}
	
		
	/**
	 * This add a newly created commit object to the list of commits for a log.
	 * @param addToList if false, the Commit will not be added to the list of commits.
	 * @return the newly created commit object.
	 */
	public Commit addCommit(boolean addToList){
		if(this.sha == null){
			throw new IllegalStateException("Can't create a commit without a SHA1 code");
		}
		Commit commit = new Commit(this.sha, this.parentSha, this.mergeDetails, this.mergeOrigin, 
			this.author, this.dateString, this.committer, this.commitDate, this.message, this.files, this.tags);
		if (addToList) {
			if (commitList == null){
				commitList = new ArrayList<Commit>();
			}
			this.commitList.add(commit);
		}
		//reset variables for future commits.

		this.sha = null;
		this.parentSha = null;
		this.mergeDetails = null;
		this.mergeOrigin = null;
		this.message = null;
		this.files = null;
		this.tags = null;
		this.author = null;
		this.dateString = null;
		this.committer = null;
		this.commitDate = null;
		
		return commit;
	
	}
	
	/**
	 * @param index 
	 *          Returns the index of error.
	 * @return The index of error.
	 */
	public ResponseString getError(int index) {
		CheckUtilities.checkIntInRange(index, 0, errors.size());
		return ( errors.get(index) );
	}
	
	/**
	 * 
	 * @return	This returns the commit list of the particular log instance. 
	 */
	public List<Commit> getLog() {
		return this.commitList;
	}
	
	public String getSha() {
		return sha;
	}
	
	/**
	 * 
	 * A data structure which  holds information about each commit.
	 *
	 */
	public static class Commit{
		
		private final String sha;
		private final String parentSha;
		private final List<String> mergeDetails; 
		private final String mergeOrigin;
		
		private final String author;
		private final String date;
		
		private final String committer;
		private final String commitDate;

		private final String message;
		
		private final String[] tags;
		//Additional Commit details
		private final List<CommitFile> files;

		int filesChanged = 0;
		int linesInserted = 0;
		int linesDeleted = 0;

		/**
		 * Constructor for creating a commit data structure.
		 * @param sha	The SHA hash for a particular commit instance. 
		 * @param mergeDetails	The Merge details for a particular commit instance. Pass null is commit is not a merge
		 * @param author	The Author for a particular commit instance.
		 * @param date	The Date of a particular commit instance.
		 * @param message	The Message for a particular commit instance.
		 * @param files	The list of files affected by a particular commit instance.
		 */
		public Commit(String sha, String parentSha, List<String> mergeDetails, String mergeOrigin, String author, String date, 
				String committer, String commitDate, String message, List<CommitFile> files, String[] tags) {
			this.sha = sha;
			this.parentSha = parentSha;
			this.mergeDetails = mergeDetails;
			this.mergeOrigin = mergeOrigin;
			this.author = author;
			this.date = date;
			this.committer = committer;
			this.commitDate = commitDate;
			this.message = message;
			this.files = files;
			this.tags = tags;
			setLinesInsertionsDeletions();
			setFilesChanged();
		}


		/**
		 * 
		 * @return This returns the SHA for each commit.
		 */
		public String getSha() {
			return sha;
		}
		
		/**
		 * 
		 * @return This returns the SHA of the parent commit.
		 */
		public String getParentSha() {
			return parentSha;
		}
		
		/**
		 * 
		 * @return This returns the merge details for each commit. If the commit was not a merge it returns null.
		 */
		public List<String> getMergeDetails() {
			return mergeDetails;
		}
		
		public String getMergeOrigin() {
			return mergeOrigin;
		}
		/**
		 * 
		 * @return This return the name of the author of the commit.
		 */
		public String getAuthor() {
			return author;
		}
		
		/**
		 * 
		 * @return This return the Date object for a particular commmit.
		 */
		public String getDateString() {
			return date;
		}
		
		public String getCommitter() {
			return committer;
		}
		
		public String getCommitDate() {
			return commitDate;
		}
		
		/**
		 * 
		 * @return	This returns the message of a commit.
		 */
		public String getMessage() {
			return message;
		}
		/**
		 *  This returns the list of files affected by a particular commit.
		 */
		public List<CommitFile> getFiles() {
			return files;
		}

		public String[] getTags() {
			return tags;
		}
		
		/**
		 * This methods calculated the total number of files affected by a particular commit.
		 */
		void setFilesChanged() {
			if(this.files != null){
				this.filesChanged = this.files.size();
			}
			else{
				this.filesChanged = 0;
			}
			
		}
		/**
		 * This calculated the Total number of lines Added and Deleted for a particular commit. 
		 */
		void setLinesInsertionsDeletions(){
			if(this.files != null){
				for(int i = 0; i< this.files.size();i++){
					this.linesInserted += files.get(i).linesAdded;
					this.linesDeleted += files.get(i).linesDeleted;
				}
				
			}
			else{
				this.linesInserted = 0;
				this.linesDeleted = 0;
			}
		}
		
		/**
		 * 
		 * @return The number of lines deleted for a paticular commit.
		 */
		public int getLinesDeleted() {
			return this.linesDeleted;
		}
		
		/**
		 * 
		 * @return @return The number of lines inserted for a particular commit.
		 */
		public int getLinesInserted() {
			return this.linesInserted;
		}
		
		/**
		 * 
		 * @return The number of files changed in a particular commit.
		 */
		public int getFilesChanged() {
			return this.filesChanged;
		}
	}

	/**
	 * 
	 * This class hold information about a file affected by a commit 
	 *
	 */
	public static class CommitFile{

		private final String oldPermissions;
		private final String permissions;
		private final String oldSha;
		private final String sha;
		private final ScmEntryChangeKind changeStatus;
		private final String originName;
		private final String filename;
		private int linesAdded = -1;
		private int linesDeleted = -1;
		
		/**
		 * Default constructor
		 * @param sha hashcode of the content of this file
		 * @param name of the file
		 * 
		 * @param originName the name of the file this file is copied/renamed from (may be null)
		 * @param oldSha the previous hashcode of the file
		 * @param changeStatus the kind of change this commitfile represents.
		 */
		public CommitFile(String oldPermissions, String permissions, String oldSha, String sha, ScmEntryChangeKind changeStatus,  String originName, String name) {
			this.oldPermissions = oldPermissions;
			this.sha = sha;
			this.originName = originName;
			this.permissions = permissions;
			this.oldSha = oldSha;
			this.filename = name;
			this.changeStatus = changeStatus;
		}
		
		/**
		 * Sets the amount of lines added in this revision, compared to the previous revision.
		 * Note that if this method is called with an argument that is larger then -1, it can't
		 * be called again. Trying to set the amount of lines added again will result in a IllegalStateException.
		 * @param linesAdded to set, may be -1 if unknown.
		 */
		public void setLinesAdded(int linesAdded) {
			if (this.linesAdded >= 0) {
				throw new IllegalStateException("Amount of linesAdded is already set on '" + this.linesAdded + "'" +
					" and can't be changed to '" + linesAdded +"'");
			}
			this.linesAdded = linesAdded;
		}
		
		/**
		 * Sets the amount of lines removed in this revision, compared to the previous revision.
		 * Note that if this method is called with an argument that is larger then -1, it can't
		 * be called again. Trying to set the amount of lines removed again will result in a IllegalStateException.
		 * @param linesDeleted to set, may be -1 if unknown.
		 */
		public void setLinesDeleted(int linesDeleted) {
			if (this.linesDeleted >= 0) {
				throw new IllegalStateException("Amount of linesDeleted is already set on '" + this.linesDeleted + "'" +
					" and can't be changed to '" + linesDeleted +"'");
			}
			this.linesDeleted = linesDeleted;
		}
		
		public String getOldPermissions() {
			return oldPermissions;
		}
		
		public String getPermissions() {
			return permissions;
		}
		
		public ScmEntryChangeKind getChangeStatus() {
			return changeStatus;
		}
		
		public String getSha() {
			return sha;
		}
		
		/**
		 * Gets the hashcode of the file, this file is copied/renamed from or
		 * if this fiel is not copied/renamed , the hashcode of the previous
		 * revision is returned.
		 * @return the previous hashcode, or NULL if none.
		 */
		public String getOldSha() {
			return oldSha;
		}
		
		/**
		 * Gets the name of the file, this file is copied/renamed from.
		 * @return the filename of the origin, or null if there is no origin.
		 */
		public String getOriginName() {
			return originName;
		}
		
		public String getName() {
			return filename;
		}
		

		/**
		 * Gets the amount of lines added in this revision, compared to the previous revision.
		 * @return the amount of lines added, or -1 if unknown.
		 */
		public int getLinesAdded() {
			return linesAdded;
		}

		/**
		 * Gets the amount of lines deleted in this revision, compared to the previous revision.
		 * @return the amount of lines deleted, or -1 if unknown.
		 */
		public int getLinesDeleted() {
			return linesDeleted;
		}
		
		
		@Override
		public String toString() {
			return "+" + linesAdded + " -" + linesDeleted + " " + filename + 
			" " + sha + "(" + oldSha + ")" + " " +  originName + " " + permissions + 
			"(" + oldPermissions + ")"; 
		}

	}
	
  public static class ResponseString {
    final String error;
         final int lineNumber;
     
    public ResponseString(int lineNumber, String error) {
      this.lineNumber = lineNumber;
      this.error = error;
    }
     
     public int getLineNumber() {
       return lineNumber;
     }
     
    public String error() {
      return error;
    }
  }

}
