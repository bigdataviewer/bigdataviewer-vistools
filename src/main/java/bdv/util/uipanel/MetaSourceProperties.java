/*-
 * #%L
 * UI for BigDataViewer.
 * %%
 * Copyright (C) 2017 - 2018 Tim-Oliver Buchholz
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bdv.util.uipanel;

import java.awt.Color;
import java.util.Set;

/**
 *
 * SourceProperties holds all information about a source added to the UI.
 *
 * @author Tim-Oliver Buchholz, CSBD/MPI-CBG Dresden
 *
 */
public class MetaSourceProperties {
	private String sourceName;
	private int sourceID;
	private String sourceType;
	private Set<String> groupNames;
	private Color color;
	private String dims;
	private boolean visibility;
	private boolean isLabeling;

	/**
	 * Information about a specific source.
	 *
	 * @param sourceName of the source
	 * @param sourceID of the source
	 * @param sourceType type
	 * @param groupNames names of the groups to which this source belongs
	 * @param color to display this source in
	 * @param dims of the source
	 * @param visibility of the source
	 * @param isLabeling if this source is a labeling
	 */
	public MetaSourceProperties(final String sourceName, final int sourceID, final String sourceType,
			final Set<String> groupNames, final Color color, final String dims, final boolean visibility,
			final boolean isLabeling) {
		this.sourceName = sourceName;
		this.sourceID = sourceID;
		this.sourceType = sourceType;
		this.groupNames = groupNames;
		this.color = color;
		this.dims = dims;
		this.visibility = visibility;
		this.isLabeling = isLabeling;
	}

	/**
	 *
	 * @return source name
	 */
	public String getSourceName() {
		return sourceName;
	}

	/**
	 *
	 * @return source ID
	 */
	public int getSourceID() {
		return sourceID;
	}

	/**
	 * Set sourceID
	 *
	 * @param id of the source
	 */
	public void setSourceID(final int id) {
		sourceID = id;
	}

	/**
	 *
	 * @return type of the source
	 */
	public String getSourceType() {
		return sourceType;
	}

	/**
	 * Names of the groups to which this source is assigned.
	 *
	 * @return names of the groups
	 */
	public Set<String> getGroupNames() {
		return groupNames;
	}

	/**
	 * Adds a group to this source.
	 *
	 * @param groupName to assign this source to
	 * @param groupID to assign this source to
	 */
	public void addGroup(final String groupName, final int groupID) {
		groupNames.add(groupName);
	}

	/**
	 * Remove group from this source.
	 *
	 * @param groupName from which this source is removed
	 * @param groupID from which this source is removed
	 */
	public void removeGroup(final String groupName, final int groupID) {
		groupNames.remove(groupName);
	}

	/**
	 *
	 * @return color of the source
	 */
	public Color getColor() {
		return color;
	}

	/**
	 *
	 * @param color
	 *            of the source
	 */
	public void setColor(final Color color) {
		this.color = color;
	}

	/**
	 *
	 * @return dimensions of the source
	 */
	public String getDims() {
		return dims;
	}

	/**
	 *
	 * @return visibility
	 */
	public boolean isVisible() {
		return visibility;
	}

	/**
	 * Set visibility.
	 *
	 * @param visibility of this source
	 */
	public void setVisible(final boolean visibility) {
		this.visibility = visibility;
	}

	/**
	 *
	 * @return labeling status
	 */
	public boolean isLabeling() {
		return isLabeling;
	}
}
