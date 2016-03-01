package xdi2.messaging.target.interceptor.impl.tor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xdi2.core.ContextNode;
import xdi2.core.Graph;
import xdi2.core.exceptions.Xdi2RuntimeException;
import xdi2.core.features.equivalence.Equivalence;
import xdi2.core.features.nodetypes.XdiCommonRoot;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.util.GraphUtil;
import xdi2.messaging.target.MessagingTarget;
import xdi2.messaging.target.Prototype;
import xdi2.messaging.target.impl.graph.GraphMessagingTarget;
import xdi2.messaging.target.interceptor.impl.AbstractInterceptor;

/**
 * This interceptor can add a Tor hidden service address as $rep equivalence relation.
 * 
 * @author markus
 */
public class TorInterceptor extends AbstractInterceptor<MessagingTarget> implements Prototype<TorInterceptor> {

	public final static int INIT_PRIORITY = 40;
	public final static int SHUTDOWN_PRIORITY = 40;

	private static final String XDI_SCHEME = ":tor:";
	private static final String DEFAULT_HOSTNAME_FILENAME = "hostname";

	private static Logger log = LoggerFactory.getLogger(TorInterceptor.class.getName());

	private List<String> hiddenServiceDirs;
	private String hostnameFileName;

	public TorInterceptor() {

		super(INIT_PRIORITY, SHUTDOWN_PRIORITY);

		this.hiddenServiceDirs = Collections.emptyList();
		this.hostnameFileName = DEFAULT_HOSTNAME_FILENAME;
	}

	/*
	 * Prototype
	 */

	@Override
	public TorInterceptor instanceFor(PrototypingContext prototypingContext) {

		// done

		return this;
	}

	/*
	 * Init and shutdown
	 */

	@Override
	public void init(MessagingTarget messagingTarget) throws Exception {

		super.init(messagingTarget);

		if (! (messagingTarget instanceof GraphMessagingTarget)) return;

		GraphMessagingTarget graphMessagingTarget = (GraphMessagingTarget) messagingTarget;
		Graph graph = graphMessagingTarget.getGraph();

		if (log.isDebugEnabled()) log.debug("hiddenServiceDir=" + this.getHiddenServiceDirs());

		// find graph owner

		ContextNode ownerContextNode = null;
		ContextNode ownerSelfPeerRootContextNode = null;

		if (log.isDebugEnabled()) log.debug("Finding graph owner: " + this.getHiddenServiceDirs());

		XDIAddress ownerXDIAddress = GraphUtil.getOwnerXDIAddress(graph);
		XDIArc ownerPeerRootXDIArc = GraphUtil.getOwnerPeerRootXDIArc(graph);

		if (ownerXDIAddress == null || ownerPeerRootXDIArc == null) throw new Xdi2RuntimeException("Graph owner not found.");

		ownerContextNode = graph.getDeepContextNode(ownerXDIAddress);
		ownerSelfPeerRootContextNode = graph.getRootContextNode().getContextNode(ownerPeerRootXDIArc);

		if (ownerContextNode == null || ownerSelfPeerRootContextNode == null) throw new Xdi2RuntimeException("Graph owner context node not found.");

		// create hidden service synonyms

		List<XDIAddress> hiddenServiceSynonyms = this.loadHiddenServiceSynonyms(ownerContextNode.getXDIArc().getCs());

		if (hiddenServiceSynonyms != null) {

			if (log.isDebugEnabled()) log.debug("Creating hidden service synonyms: " + hiddenServiceSynonyms);

			for (XDIAddress hiddenServiceSynonym : this.loadHiddenServiceSynonyms(ownerContextNode.getXDIArc().getCs())) {

				ContextNode hiddenServiceSynonymContextNode = graph.setDeepContextNode(hiddenServiceSynonym);
				Equivalence.setReplacementContextNode(hiddenServiceSynonymContextNode, ownerContextNode, false);

				ContextNode hiddenServiceSynonymPeerRootContextNode = XdiCommonRoot.findCommonRoot(graph).getPeerRoot(hiddenServiceSynonym, true).getContextNode();
				Equivalence.setReplacementContextNode(hiddenServiceSynonymPeerRootContextNode, ownerSelfPeerRootContextNode, false);
			}
		}
	}

	@Override
	public void shutdown(MessagingTarget messagingTarget) throws Exception {

		super.shutdown(messagingTarget);
	}

	public List<XDIAddress> loadHiddenServiceSynonyms(Character cs) {

		List<XDIAddress> hiddenServiceSynonyms = new ArrayList<XDIAddress> ();
		String hostnameFileName = this.getHostnameFileName();

		for (String hiddenServiceDir : this.getHiddenServiceDirs()) {

			String hostname;

			// read hostname

			File file = new File(new File(hiddenServiceDir), hostnameFileName);
			BufferedReader reader = null;

			try {

				reader = new BufferedReader(new FileReader(file));
				hostname = reader.readLine();
				reader.close();
			} catch (IOException ex) {

				if (log.isErrorEnabled()) log.error("Unable to read hostname file in " + hiddenServiceDir + ": " + ex.getMessage(), ex);
				continue;
			} finally {

				if (reader != null) {

					try {

						reader.close();
					} catch (IOException ex) { 

						if (log.isErrorEnabled()) log.error("Unable to close hostname file in " + hiddenServiceDir + ": " + ex.getMessage(), ex);
					}
				}
			}

			// build hidden service synonym

			XDIAddress hiddenServiceSynonym = XDIAddress.fromComponent(
					XDIArc.fromComponents(
							cs, 
							false, 
							false, 
							false, 
							false, 
							true, 
							false, 
							XDI_SCHEME + hostname.substring(0, hostname.indexOf(".onion")), 
							null));

			// add it to the list

			hiddenServiceSynonyms.add(hiddenServiceSynonym);
		}

		// done

		return hiddenServiceSynonyms;
	}

	/*
	 * Getters and setters
	 */

	public List<String> getHiddenServiceDirs() {

		return this.hiddenServiceDirs;
	}

	public void setHiddenServiceDirs(List<String> hiddenServiceDir) {

		this.hiddenServiceDirs = hiddenServiceDir;
	}

	public String getHostnameFileName() {

		return this.hostnameFileName;
	}

	public void setHostnameFileName(String hostnameFileName) {

		this.hostnameFileName = hostnameFileName;
	}
}
