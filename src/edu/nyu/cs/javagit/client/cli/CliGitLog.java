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
package edu.nyu.cs.javagit.client.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.nyu.cs.javagit.api.JavaGitConfiguration;
import edu.nyu.cs.javagit.api.JavaGitException;
import edu.nyu.cs.javagit.api.commands.CommandResponse;
import edu.nyu.cs.javagit.api.commands.GitLogOptions;
import edu.nyu.cs.javagit.api.commands.GitLogResponse;
import edu.nyu.cs.javagit.api.commands.GitLogResponse.Commit;
import edu.nyu.cs.javagit.api.commands.GitLogResponse.CommitFile;
import edu.nyu.cs.javagit.client.IGitLog;
import edu.nyu.cs.javagit.utilities.CheckUtilities;
import experiments.scm.ScmEntryChangeKind;
import experiments.scm.ScmEntryChangeKind.GitChangeKind;
import experiments.scm.ScmLogEntryHandler;

/**
 * Command-line implementation of the <code>IGitLog</code> interface.
 */
public class CliGitLog implements IGitLog{
	
	/**
	 * Implementations of &lt;git log&gt; with options and ScmLogEntryHandler.
	*/
	public List<Commit> log(File repositoryPath, GitLogOptions options, ScmLogEntryHandler<Commit> handler)
	throws JavaGitException, IOException {
		CheckUtilities.checkFileValidity(repositoryPath);
		GitLogParser parser = new GitLogParser(handler);
		List<String> command = buildCommand(repositoryPath, options);
		GitLogResponse response =  (GitLogResponse) ProcessUtilities.runCommand(repositoryPath,
				command, parser);
		if (response.containsError()) {
			int line = response.getError(0).getLineNumber();
			String error = response.getError(0).error();
			throw new JavaGitException(420001, "Line " + line + ", " + error);
		}
		return response.getLog();
	}
	
	/**
	 * Implementations of &lt;git log&gt; with options and one file to be added to index.
	 */
	public List<Commit> log(File repositoryPath, GitLogOptions options)
	throws JavaGitException, IOException {
		CheckUtilities.checkFileValidity(repositoryPath);
		GitLogParser parser = new GitLogParser();
		List<String> command = buildCommand(repositoryPath, options);
		GitLogResponse response =  (GitLogResponse) ProcessUtilities.runCommand(repositoryPath,
				command, parser);
		if (response.containsError()) {
			int line = response.getError(0).getLineNumber();
			String error = response.getError(0).error();
			throw new JavaGitException(420001, "Line " + line + ", " + error);
		}
		return response.getLog();
	}

	public List<Commit> log(File repositoryPath) throws JavaGitException,
	IOException {
		CheckUtilities.checkFileValidity(repositoryPath);
		GitLogParser parser = new GitLogParser();
		List<String> command = buildCommand(repositoryPath, null);
		GitLogResponse response =  (GitLogResponse) ProcessUtilities.runCommand(repositoryPath,
				command, parser);
		if (response.containsError()) {
			int line = response.getError(0).getLineNumber();
			String error = response.getError(0).error();
			throw new JavaGitException(420001, "Line " + line + ", " + error);
		}
		return response.getLog();
	}

	/**
	 * This function builds the git log commands with necessary options as specified by the user.
	 * @param repositoryPath Root of the repository
	 * @param options	Options supplied to the git log command using <code>GitLogOptions</code>.
	 * @return Returns a List of command argument to be applied to git log.
	 */
	private List<String> buildCommand(File repositoryPath, GitLogOptions options) {
		List<String> command = new ArrayList<String>();
		command.add(JavaGitConfiguration.getGitCommand());
		command.add("log");
		command.add("--decorate=full");
		command.add("--pretty=fuller");
		command.add("--parents");
		if(options!=null){
			//General Options
			/**
			 * Breaks rewrite changes in to pairs of delete and create.
			 */
			if (options.isOptBreakRewriteChanges()) {
				command.add("-B");
			}
			/**
			 * Detects renames
			 */
			if (options.isOptDetectRenames()) {
				command.add("-M");
			}
			/**
			 * Detects copies and renames, of original files
			 */
			if (options.isOptFindCopies()) {
				command.add("-C");
			}
			/**
			 * 	Detects copies and renames , very expensive operation.
			 */
			if (options.isOptFindCopiesHarder()) {
				command.add("--find-copies-harder");
			}
			
			/**
			 * Only gets the logs if in the range.
			 */
			if (options.isOptLimitRange()) {
				command.add(options.getOptLimitRangePattern());
			}
			
			/**
			 * Pretend as if all the refs in refs/ are listed on the command line as <commit>.
			 */
			if (options.isOptLimitAll()) {
				command.add("--all");
			}
			
			/**
			 *  List details about lines modified and files affected in a commit.
			 */
			if (options.isOptFileDetails()) {
				command.add("--numstat");
				command.add("--raw");
			}
			
			
			if (options.isOptMergeDetails()) {
				command.add("-m");
			}

			/**
			 * 	List all logs on the relative path.
			 */
			if (options.isOptRelative()) {
				command.add("--relative="+options.getOptRelativePath());
			}

			/**
			 * 	List all logs since specified date.
			 */
			if (options.isOptLimitCommitSince()) {
				command.add("--since="+ options.getOptLimitSince());
			}

			/**
			 * 	List all logs after specified date.
			 */
			if (options.isOptLimitCommitAfter()) {
				command.add("--after="+ options.getOptLimitAfter());
			}

			/**
			 * 	List all logs after specified date.
			 */
			if (options.isOptLimitCommitUntil()) {
				command.add("--until="+ options.getOptLimitUntil());
			}

			/**
			 * 	List all logs before specified date.
			 */
			if (options.isOptLimitCommitBefore()) {
				command.add("--before="+ options.getOptLimitBefore());
			}

			/**
			 * 	List all logs by an author
			 */
			if (options.isOptLimitAuthor()) {
				command.add("--author="+ options.getOptAuthor());
			}

			/**
			 * 	List all logs by an author/committer header pattern.
			 */
			if (options.isOptLimitCommitterPattern()) {
				command.add("--committer="+ options.getOptLimitPattern());
			}

			/**
			 * 	List all logs by matching to a grep pattern.
			 */
			if (options.isOptLimitGrep()) {
				command.add("--grep="+ options.getOptLimitGrepPattern().toString());
			}
			/**
			 * 	Match regular expressions with out  regard to letters case.
			 */
			if (options.isOptLimitMatchIgnoreCase()) {
				command.add("-i");
			}

			/**
			 * 	Match extended regular expressions.
			 */
			if (options.isOptLimitEnableExtendedRegex()) {
				command.add("-E");
			}

			/**
			 * 	Match patterns as fixed strings and not regular expressions.
			 */
			if (options.isOptLimitMatchIgnoreCase()) {
				command.add("-F");
			}

			/**
			 * 	Stop when a path dissapears from the tree.
			 */
			if (options.isOptLimitRemoveEmpty()) {
				command.add("--remove-empty");
			}

			/**
			 * 	Match parts of history irrelevant to the current path.
			 */
			if (options.isOptLimitFullHistory()) {
				command.add("--full-history");
			}

			/**
			 * 	Do not print commits with more than one parent.
			 */
			if (options.isOptLimitNoMerges()) {
				command.add("--no-merges");
			}
			
			/**
			 * Print only merges
			 */
			if (options.isOptLimitOnlyMerges()) {
				command.add("--merges");
			}

			/**
			 * 	Follow only first parent on seeing a merge.
			 */
			if (options.isOptLimitFirstParent()) {
				command.add("--first-parent");
			}

			/**
			 * 	Order commits topologically.
			 */
			if (options.isOptOrderingTopological()) {
				command.add("--topo-order");
			}

			/**
			 * 	Order commits in reverse
			 */
			if (options.isOptOrderingReverse()) {
				command.add("--reverse");
			}
			
			/**
			 * 	Limits the number of commits to retrieve.
			 */
			if (options.isOptLimitCommitMax()) {
				command.add("-n");
				command.add(String.valueOf(options.getOptLimitMax()));
			}
			
			// Skips the specified number of commit messages
			if (options.isOptLimitCommitSkip()) {
			  command.add("--skip=" + options.getOptLimitSkip());
			}
		}

		return command;

	}

	/**
	 * Parser class to parse the output generated by git log; and return a
	 * <code>GitLogResponse</code> object.
	 */
	public class GitLogParser implements IParser {

		//private int linesAdded = 0;
		//private int linesDeleted = 0;
		private boolean canCommit = false;
		private String filename = null;
		private String []tmp ;
		private LinkedList<CommitFile> tmpCommitFiles = new LinkedList<CommitFile>();
		private GitLogResponse response = new GitLogResponse();
		private final ScmLogEntryHandler<Commit> handler;
		
		public GitLogParser(ScmLogEntryHandler<Commit> handler) {
			this.handler = handler;
		}
		
		public GitLogParser() {
			this.handler = null;
		}
		/**
		 * Add the final parsed commit. and returns the response of git log execution.
		 */
		public CommandResponse getResponse() throws JavaGitException {
			if (response.getSha() != null) {
				//if we had one last commit not processed.
				processCommit();	
			}
			return this.response;
		}
		
		private void processCommit() {
			Commit commited = response.addCommit(handler == null);
			if (handler != null) {
				handler.handleLogEntry(commited);
			}
		}

		public void parseLine(String line, long number) {
			System.out.println(number + ")\t" + line);
			if(line.length() == 0){
				return;
			} else if (line.startsWith("commit")){
				
			} else if (line.startsWith("parent")) {
				
			} else if (line.startsWith("Merge")){
				
			} else if (line.startsWith("Date") || line.startsWith("AuthorDate")) {
				
			} else if (line.startsWith("Author")) {
				
			} else if (line.startsWith("CommitDate")) {
				
			} else if (line.startsWith("Commit")) {
				
			} else {
				tmp = line.split("\t|\n|\r|\f| ");
				if (tmp.length > 0) {
					String first = tmp[0];
					if (first.startsWith(":") && tmp.length >= 6) {
						
					} else if (tmp.length >= 3 && tmpCommitFiles.size() > 0) {
						//4       0       kok/{kokok.txt => hah.txt}
						
						try {
							Integer.parseInt(tmp[0]);
							Integer.parseInt(tmp[1]);
						} catch (NumberFormatException e) {
							System.out.println("parseLine " + number +  ":" + line);
							System.out.println("tmp (" + tmp.length + "):" + tmp[0] + " " + tmp[1]);
							throw e;
						}
					} else {
						
					}
				}
			}
		}

		/**
		 *  Parses a line at a time from the commandline execution output of git log
		 */    		
		public void parseLine(String line) {
			if(line.length() == 0){
				return;
			}

			//commit
			if (line.startsWith("commit")){
				if(canCommit){
					processCommit();
				}
				canCommit = true;
				int start = line.indexOf(' ') + 1;
				int end = line.indexOf(' ', start);

				String sha;
				if (end > 0) {
					sha = line.substring(start, end);
					
					int fromEnd = end;
					int fromStart = line.indexOf("(from ", end) + 1;
					if (fromStart > 0) {
						fromEnd = line.indexOf(')', fromStart);
						String fromCommit = line.substring(fromStart + 5, fromEnd);
						response.setMergeOrigin(fromCommit);
					}
					
					int tagsStart = line.indexOf('(', fromEnd) + 1;
					if (tagsStart > 0) {
						if (fromStart == 0) {
							fromStart = tagsStart;
						}
						int tagsEnd = line.indexOf(')', tagsStart);
						if (tagsEnd > 0) {
							String tags = line.substring(tagsStart, tagsEnd);
							String tagNames[] = tags.split(",");
							for (int i = 0; i < tagNames.length; i++) {
								String t = tagNames[i].trim();
								if (t.startsWith("tag: ")) {
									tagNames[i] = t.substring(5);
								} else {
									tagNames[i] = t;
								}
							}
							response.setTags(tagNames);
						}
					}
/**
 * commit 9580c0126d69dff0bac8976cacc89506713a14c4 c9e1510739a38402b9d734f9d8e98da497265f6d 49fb1114ca771c44d1f98fef3277d5ae8f7e4733 (HEAD, tag: v2.6.34-rc7, ok, master)
	Merge: c9e1510 49fb111
	Author:     Waruzjan <w.shahbazian@cwi.nl>
	AuthorDate: Fri Jun 25 15:33:11 2010 +0200
	Commit:     Waruzjan <w.shahbazian@cwi.nl>
	CommitDate: Fri Jun 25 15:33:11 2010 +0200
	
	    Merge branch 'exper'
	
	commit ff3edb15951f032d235e95cc46f9adc98557b7a6 2cc2610298ca3840c4819db0c2d5776b0ec0fa8f 4ac1193dedfca1c80887607623204a1e088a55d3
	Merge: 2cc2610 4ac1193
	Author:     Waruzjan <w.shahbazian@cwi.nl>
	AuthorDate: Fri May 21 15:25:20 2010 +0200
	Commit:     Waruzjan <w.shahbazian@cwi.nl>
	CommitDate: Fri May 21 15:25:20 2010 +0200

 */
					int parentsEnd = (fromStart > 0) ? fromStart - 1 : line.length();
					
					if (parentsEnd - end > 1) {
						//there are parents
						String[] parents = line.substring(end, parentsEnd).trim().split(" ");
						for (String parentSha : parents) {
							response.addParentSha(parentSha);
						}
					}
//					
//					if (tagsStart > 0 && tagsStart - end > 1) {
//						//there are parents
//						String[] parents = line.substring(end, tagsStart).trim().split(" ");
//						for (String parentSha : parents) {
//							response.addParentSha(parentSha);
//						}
//					}
					
					/*int parent = line.indexOf(' ', end + 1);
					String parentSha = null;
					//TODO TEST!					
					if (parent > tagsStart && tagsStart > 0) {
						//nothing
					} else	if (parent > 0) { //multiple parents or there is a parent and tags
						//commit ff3edb15951f032d235e95cc46f9adc98557b7a6 2cc2610298ca3840c4819db0c2d5776b0ec0fa8f 4ac1193dedfca1c80887607623204a1e088a55d 
						//commit ff3edb15951f032d235e95cc46f9adc98557b7a6 2cc2610298ca3840c4819db0c2d5776b0ec0fa8f 4ac1193dedfca1c80887607623204a1e088a55d  (HEAD, exper)
						parentSha = line.substring(end + 1, parent);
					} else if (tagsStart <= 0) { //no tags, just one parent
						//commit ff3edb15951f032d235e95cc46f9adc98557b7a6 2cc2610298ca3840c4819db0c2d5776b0ec0fa8f
						parentSha = line.substring(end + 1);
					}
					
					if (parentSha != null) {
						response.addParentSha(parentSha);
						//System.out.println("Parent of '" + sha + "' is '" + parentSha + "'");
					}*/
					
				} else {
					//no parents or refs
					sha = line.substring(start);
				}
				response.setSha(sha);
			}
			//parent
			else if (line.startsWith("parent")) {
				tmp = line.split(" ");
				response.addParentSha(tmp[1]);
			}
			//merge (optional)
			else if (line.startsWith("Merge") && !response.hasMergeDetails()){
				
				List<String> mergeDetails = new ArrayList<String>();
				tmp = line.split(" ");
				/*StringTokenizer st = new StringTokenizer(line.substring(tmp[0].length()));
				while(st.hasMoreTokens()){
					mergeDetails.add(st.nextToken().trim());
				}*/
				for (int i = 1; i < tmp.length; i++) {
					String sha = tmp[i].trim();
					if (sha.length() > 0) {
						mergeDetails.add(sha);
					}
				}

				response.setMergeDetails(mergeDetails);
				mergeDetails = null;
			}
			//Date , note that ordering is important (Author after the AuthorDate)
			else if (line.startsWith("Date") || line.startsWith("AuthorDate")){
				response.setDate(line.substring(line.indexOf(' ')).trim());
			}
			//Author
			else if (line.startsWith("Author")){
				response.setAuthor(line.substring(line.indexOf(' ')).trim());
			}
			//CommitterDate, note that ordering is important (Commit after the CommitDate)
			else if (line.startsWith("CommitDate")) {
				response.setCommitDate(line.substring(line.indexOf(' ')).trim());
			}
			//Committer
			else if (line.startsWith("Commit")) {
				response.setCommitter(line.substring(line.indexOf(' ')).trim());
			}
			//message or fileDetails (always starts with an int)
			else {
				/**
 					commit 0ac03ea85f5bd8e512e4ca77b01a0191f1050208 (HEAD, refs/tags/v2.6.17-rc2, refs/heads/master)
					Author: Waruzjan <w.shahbazian@cwi.nl>
					Date:   Fri May 21 11:29:28 2010 +0200
					
					    eens kijken
					
					:100644 100644 f792531... 575f1eb... C081       kok/kokok.txt   kok/hah.txt
					:100644 100644 f792531... e1b2ab7... M  kok/kokok.txt
					:000000 100644 0000000... 178142b... A  firstFile.txt
					4       0       kok/{kokok.txt => hah.txt}
					5       0       kok/kokok.txt

				 */

				tmp = line.split("\t|\n|\r|\f| ");
				if (tmp.length > 0) {
					String first = tmp[0];
					if (first.startsWith(":") && tmp.length >= 6) {
						//:100644 100644 f792531... 575f1eb... C081       kok/kokok.txt   kok/hah.txt
						int pos = 0;
						String oldPermissions = tmp[pos++].substring(1);
						if (oldPermissions.equals("000000")) {
							oldPermissions = null;
						}
						String permissions = tmp[pos++];
						if (permissions.equals("000000")) {
							permissions = null;
						}//TODO test the ... or .. cases of short sha
						String originHashcode = tmp[pos++];
						if (originHashcode.endsWith(".")) {
							originHashcode = originHashcode.substring(0, originHashcode.indexOf('.'));
						}
						if (originHashcode.equals("0000000")) {
							originHashcode = null;
						}
						String hashCode = tmp[pos++];
						if (hashCode.endsWith(".")) {
							hashCode = hashCode.substring(0, hashCode.indexOf('.'));
						}
						if (hashCode.equals("0000000")) {
							hashCode = null;
						}
						ScmEntryChangeKind status = GitChangeKind.from(tmp[pos++]);
						String originName = null;
						if (tmp.length > pos+1) {
							originName = tmp[pos++];
						}
						filename = tmp[pos++];											 
						tmpCommitFiles.add(new CommitFile(oldPermissions, permissions, originHashcode, 
							hashCode, status, originName, filename));
					} else if (tmp.length >= 3 && tmpCommitFiles.size() > 0) {
						//4       0       kok/{kokok.txt => hah.txt}
						
						CommitFile commitFile = tmpCommitFiles.getFirst();
						String linesAdded = tmp[0];
						String linesRemoved = tmp[1];
						if (!linesAdded.equals("-")) {
							try {
								commitFile.setLinesAdded(Integer.parseInt(linesAdded));
							} catch (NumberFormatException e) {
								System.err.println("Can't parse the lines added (" + linesAdded + ") from line'" + line + "'");
								throw e;
							}
						}
						if (!linesRemoved.equals("-")) {
							try {
								commitFile.setLinesDeleted(Integer.parseInt(linesRemoved));
							} catch (NumberFormatException e) {
								System.err.println("Can't parse the lines deleted (" + linesRemoved + ") from line'" + line + "' ");
								throw e;
							}
						}
						response.addFile(commitFile);
						tmpCommitFiles.removeFirst();
					} else {
						response.setMessage(line);
					}
				}
				//if (splitted.length == 0) {
				//	System.out.println("!!" + line);
				//}
				/*int pos = 0;
				try{
					if (splitted[0].equals("-")) {
						pos++;
					}
					Status fileStatus = CommitFile.parseStatus(splitted[pos]);
					if (fileStatus != null) {
						filename = splitted[++pos];
						if (splitted.length > pos) {
							//TODO fix the bug where { is part of the path/file name
							String oldFilename = filename;
							String filename = splitted[++pos];
							response.addFile(filename, -1, -1, oldFilename);
						} else {
							response.addFile(filename,-1,-1);
						}
					} else {
						String added = splitted[pos++];
						linesAdded = Integer.parseInt(added);
						linesDeleted = Integer.parseInt(splitted[pos++]);
						filename = splitted[pos++];
						if (splitted.length > pos+1 && splitted[pos].equals("=>")) {
							//TODO fix the bug where { is part of the path/file name
							String newFilename = splitted[++pos];
							response.addFile(newFilename, linesAdded, linesDeleted, filename);
						} else {
							response.addFile(filename,linesAdded,linesDeleted);
						}
					}
				}
				catch(NumberFormatException nfe){
					response.setMessage(line);
				}
				catch(Exception e){
					response.setMessage(line);
				}*/
			}
		}


		public void processExitCode(int code) {
			// TODO Auto-generated method stub
			
		}
	}
}
