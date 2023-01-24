/*******************************************************************************
 * Copyright (c) 2004, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package io.sloeber.schema.api;

import org.eclipse.cdt.core.settings.model.extension.CTargetPlatformData;

import io.sloeber.schema.internal.IBuildObject;

/**
 * This class defines the os/architecture combination upon which the
 * outputs of a tool-chain can be deployed. The osList and archList
 * attributes contain the Eclipse names of the operating systems and
 * architectures described by this element.
 *
 * @since 2.1
 * @noextend This class is not intended to be subclassed by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITargetPlatform extends IBuildObject {
    public static final String TARGET_PLATFORM_ELEMENT_NAME = "targetPlatform"; //$NON-NLS-1$
    public static final String BINARY_PARSER = "binaryParser"; //$NON-NLS-1$
    public static final String OS_LIST = "osList"; //$NON-NLS-1$
    public static final String ARCH_LIST = "archList"; //$NON-NLS-1$

    /**
     * Returns the tool-chain that is the parent of this target platform.
     *
     * @return IToolChain
     */
    public IToolChain getParent();

    /**
     * Returns the <code>ITargetPlatform</code> that is the superclass of this
     * target platform, or <code>null</code> if the attribute was not specified.
     *
     * @return ITargetPlatform
     */
    public ITargetPlatform getSuperClass();

    /**
     * Returns whether this element is abstract. Returns <code>false</code>
     * if the attribute was not specified.
     *
     * @return boolean
     */
    public boolean isAbstract();

    /**
     * Sets the isAbstract attribute of the target paltform.
     */
    public void setIsAbstract(boolean b);

    /**
     * Returns an array of operating systems this target platform represents.
     *
     * @return String[]
     */
    public String[] getOSList();

    /**
     * Sets the OS list.
     *
     * @param OSs
     *            The list of OS names
     */
    public void setOSList(String[] OSs);

    /**
     * Returns an array of architectures this target platform represents.
     *
     * @return String[]
     */
    public String[] getArchList();

    /**
     * Sets the architecture list.
     *
     * @param archs
     *            The list of architecture names
     */
    public void setArchList(String[] archs);

    /**
     * Returns the unique IDs of the binary parsers associated with the target
     * platform.
     *
     * @return String[]
     */
    public String[] getBinaryParserList();

    /**
     * Sets the string ids of the binary parsers for this target platform.
     */
    public void setBinaryParserList(String[] ids);

    /**
     * Returns <code>true</code> if this element has changes that need to
     * be saved in the project file, else <code>false</code>.
     *
     * @return boolean
     */
    public boolean isDirty();

    /**
     * Sets the element's "dirty" (have I been modified?) flag.
     */
    public void setDirty(boolean isDirty);

    /**
     * Returns <code>true</code> if this target platform was loaded from a manifest
     * file,
     * and <code>false</code> if it was loaded from a project (.cdtbuild) file.
     *
     * @return boolean
     */
    public boolean isExtensionElement();

    public CTargetPlatformData getTargetPlatformData();

}
