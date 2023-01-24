/*******************************************************************************
 * Copyright (c) 2007, 2016 Intel Corporation and others.
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
 * cartu38 opendev (STMicroelectronics) - [514385] Custom defaultValue-generator support
 *******************************************************************************/
package io.sloeber.schema.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.settings.model.ICSettingBase;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.extension.CFolderData;
import org.eclipse.cdt.core.settings.model.extension.CLanguageData;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import io.sloeber.autoBuild.api.BuildException;
import io.sloeber.autoBuild.api.IModificationStatus;
import io.sloeber.autoBuild.core.Activator;
import io.sloeber.schema.api.IConfiguration;
import io.sloeber.schema.api.IFolderInfo;
import io.sloeber.schema.api.IHoldsOptions;
import io.sloeber.schema.api.IInputType;
import io.sloeber.schema.api.IManagedProject;
import io.sloeber.schema.api.IOption;
import io.sloeber.schema.api.IOutputType;
import io.sloeber.schema.api.ITargetPlatform;
import io.sloeber.schema.api.ITool;
import io.sloeber.schema.api.IToolChain;

public class FolderInfo extends BuildObject implements IFolderInfo {
    private String id;
    private String name;
    private Configuration myParent;
    private boolean myIsExcluded;

    String[] modelResourcePath;
    String[] modelExclude;

    public FolderInfo(IConfiguration parent, IExtensionPoint root, IConfigurationElement element) {

        myParent = (Configuration) parent;

        loadNameAndID(root, element);

        modelResourcePath = getAttributes(RESOURCEPATH);
        modelExclude = getAttributes(EXCLUDE);

        //		IConfigurationElement tcEl = null;
        //		if (!hasBody) {
        //			// setPath(Path.ROOT);
        //			id = (ManagedBuildManager.calculateChildId(parent.getId(), null));
        //			name = ("/"); //$NON-NLS-1$
        //			tcEl = element;
        //		} else {
        //			IConfigurationElement children[] = element.getChildren(IToolChain.TOOL_CHAIN_ELEMENT_NAME);
        //			if (children.length > 0)
        //				tcEl = children[0];
        //		}

        //        if (tcEl != null)
        //            toolChain = new ToolChain(this, root, tcEl);

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isExcluded(IFile file) {
        return false;
    }

    @Override
    public Configuration getConfiguration() {
        return myParent;
    }

    @Override
    public ToolChain getToolChain() {
        return (ToolChain) myParent.getToolChain();
    }

    @Override
    public IPath getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    //    private boolean conflictsWithRootTools(ITool tool) {
    //        IFolderInfo rf = getParent().getRootFolderInfo();
    //        List<ITool> rootTools = rf.getFilteredTools();
    //        ITool tt = getParent().getTargetTool();
    //        for (ITool rootTool : rootTools) {
    //            if (rootTool == tt || getMultipleOfType(rootTool) != null) {
    //                if (getConflictingInputExts(rootTool, tool).length != 0)
    //                    return true;
    //            }
    //        }
    //        return false;
    //    }

    //    private IInputType getMultipleOfType(ITool tool) {
    //        return null;
    //        //        IInputType[] types = tool.getInputTypes();
    //        //        IInputType mType = null;
    //        //        boolean foundNonMultiplePrimary = false;
    //        //        for (IInputType type : types) {
    //        //            if (type.getMultipleOfType()) {
    //        //                if (type.getPrimaryInput() == true) {
    //        //                    foundNonMultiplePrimary = false;
    //        //                    mType = type;
    //        //                    break;
    //        //                } else if (mType == null) {
    //        //                    mType = type;
    //        //                }
    //        //            } else {
    //        //                if (type.getPrimaryInput() == true) {
    //        //                    foundNonMultiplePrimary = true;
    //        //                }
    //        //            }
    //        //        }
    //        //
    //        //        return foundNonMultiplePrimary ? null : mType;
    //    }

    //    public List<ITool> filterTools(List<ITool> localTools, IManagedProject manProj) {
    //        if (manProj == null) {
    //            // If this is not associated with a project, then there is nothing to filter
    //            // with
    //            return localTools;
    //        }
    //        IProject project = (IProject) manProj.getOwner();
    //        List<ITool> tools = new ArrayList<>(localTools.size());
    //        for (ITool t : localTools) {
    //            Tool tool = (Tool) t;
    //            if (!tool.isEnabled(this))
    //                continue;
    //
    //            if (!isRoot() && conflictsWithRootTools(tool))
    //                continue;
    //
    //            try {
    //                // Make sure the tool is right for the project
    //                switch (tool.getNatureFilter()) {
    //                case ITool.FILTER_C:
    //                    if (project.hasNature(CProjectNature.C_NATURE_ID)
    //                            && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
    //                        tools.add(tool);
    //                    }
    //                    break;
    //                case ITool.FILTER_CC:
    //                    if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
    //                        tools.add(tool);
    //                    }
    //                    break;
    //                case ITool.FILTER_BOTH:
    //                    tools.add(tool);
    //                    break;
    //                default:
    //                    break;
    //                }
    //            } catch (CoreException e) {
    //                continue;
    //            }
    //        }
    //
    //        // Answer the filtered tools as an array
    //        return tools;
    //    }
    //
    //    @Override
    //    public List<ITool> getFilteredTools() {
    //        if (toolChain == null) {
    //            return new ArrayList<ITool>();
    //        }
    //        List<ITool> localTools = toolChain.getTools();
    //        IManagedProject manProj = getParent().getManagedProject();
    //        return filterTools(localTools, manProj);
    //    }
    //
    //    @Override
    //    public final int getKind() {
    //        return ICSettingBase.SETTING_FOLDER;
    //    }
    //
    //    @Override
    //    public IToolChain getToolChain() {
    //        return toolChain;
    //    }
    //
    //    @Override
    //    public List<ITool> getTools() {
    //        return toolChain.getTools();
    //    }
    //
    //    @Override
    //    public ITool getTool(String id) {
    //        return toolChain.getTool(id);
    //    }
    //
    //
    //    ToolChain createToolChain(IToolChain superClass, String Id, String name, boolean isExtensionElement) {
    //        toolChain = new ToolChain(this, superClass, Id, name, isExtensionElement);
    //        return toolChain;
    //    }

    //    public String getErrorParserIds() {
    //        if (toolChain != null)
    //            return toolChain.getErrorParserIds(getParent());
    //        return null;
    //    }

    //    @Override
    //    public CFolderData getFolderData() {
    //        return myBuildFolderData;
    //    }
    //
    //    @Override
    //    public CLanguageData[] getCLanguageDatas() {
    //        List<CLanguageData> list = new ArrayList<>();
    //        for (ITool t : getFilteredTools()) {
    //            CLanguageData[] tt = t.getCLanguageDatas();
    //            if (tt != null) {
    //                for (CLanguageData d : tt)
    //                    list.add(d);
    //            }
    //        }
    //        return list.toArray(new BuildLanguageData[list.size()]);
    //    }

    //    @Override
    //    public ITool getToolFromOutputExtension(String extension) {
    //        // Treat a null argument as an empty string
    //        String ext = extension == null ? "" : extension; //$NON-NLS-1$
    //        // Get all the tools for the current config
    //       List< ITool> tools = getFilteredTools();
    //        for (ITool tool : tools) {
    //            if (tool.producesFileType(ext)) {
    //                return tool;
    //            }
    //        }
    //        return null;
    //    }

    //    @Override
    //    public ITool getToolFromInputExtension(String sourceExtension) {
    //        return null;
    //        // Get all the tools for the current config
    //        //        ITool[] tools = getFilteredTools();
    //        //        for (ITool tool : tools) {
    //        //            if (tool.buildsFileType(sourceExtension)) {
    //        //                return tool;
    //        //            }
    //        //        }
    //        //        return null;
    //    }

    //    public void checkPropertiesModificationCompatibility(final ITool tools[],
    //            Map<String, String> unspecifiedRequiredProps, Map<String, String> unspecifiedProps,
    //            Set<String> undefinedSet) {
    //        //		final ToolChain tc = (ToolChain) getToolChain();
    //        //		IBuildPropertiesRestriction r = new IBuildPropertiesRestriction() {
    //        //			@Override
    //        //			public boolean supportsType(String typeId) {
    //        //				if (tc.supportsType(typeId, false))
    //        //					return true;
    //        //
    //        //				for (ITool tool : tools) {
    //        //					if (((Tool) tool).supportsType(typeId))
    //        //						return true;
    //        //				}
    //        //				return false;
    //        //			}
    //        //
    //        //			@Override
    //        //			public boolean supportsValue(String typeId, String valueId) {
    //        //				if (tc.supportsValue(typeId, valueId, false))
    //        //					return true;
    //        //
    //        //				for (ITool tool : tools) {
    //        //					if (((Tool) tool).supportsValue(typeId, valueId))
    //        //						return true;
    //        //				}
    //        //				return false;
    //        //			}
    //        //
    //        //			@Override
    //        //			public String[] getRequiredTypeIds() {
    //        //				List<String> list = new ArrayList<>();
    //        //
    //        //				list.addAll(Arrays.asList(tc.getRequiredTypeIds(false)));
    //        //
    //        //				for (ITool tool : tools) {
    //        //					list.addAll(Arrays.asList(((Tool) tool).getRequiredTypeIds()));
    //        //				}
    //        //
    //        //				return list.toArray(new String[list.size()]);
    //        //			}
    //        //
    //        //			@Override
    //        //			public String[] getSupportedTypeIds() {
    //        //				List<String> list = new ArrayList<>();
    //        //
    //        //				list.addAll(Arrays.asList(tc.getSupportedTypeIds(false)));
    //        //
    //        //				for (ITool tool : tools) {
    //        //					list.addAll(Arrays.asList(((Tool) tool).getSupportedTypeIds()));
    //        //				}
    //        //
    //        //				return list.toArray(new String[list.size()]);
    //        //			}
    //        //
    //        //			@Override
    //        //			public String[] getSupportedValueIds(String typeId) {
    //        //				List<String> list = new ArrayList<>();
    //        //
    //        //				list.addAll(Arrays.asList(tc.getSupportedValueIds(typeId, false)));
    //        //
    //        //				for (ITool tool : tools) {
    //        //					list.addAll(Arrays.asList(((Tool) tool).getSupportedValueIds(typeId)));
    //        //				}
    //        //
    //        //				return list.toArray(new String[list.size()]);
    //        //			}
    //        //
    //        //			@Override
    //        //			public boolean requiresType(String typeId) {
    //        //				if (tc.requiresType(typeId, false))
    //        //					return true;
    //        //
    //        //				for (ITool tool : tools) {
    //        //					if (((Tool) tool).requiresType(typeId))
    //        //						return true;
    //        //				}
    //        //				return false;
    //        //			}
    //        //		};
    //        //
    //        //		checkPropertiesModificationCompatibility(r, unspecifiedRequiredProps, unspecifiedProps, undefinedSet);
    //    }

    //    public boolean checkPropertiesModificationCompatibility(IToolChain tc, Map<String, String> unspecifiedRequiredProps,
    //            Map<String, String> unspecifiedProps, Set<String> undefinedSet) {
    //        return false;// checkPropertiesModificationCompatibility((IBuildPropertiesRestriction) tc, unspecifiedRequiredProps,
    //        //	unspecifiedProps, undefinedSet);
    //    }

    //    public boolean isPropertiesModificationCompatible(IToolChain tc) {
    //        return false;
    //        //		Map<String, String> requiredMap = new HashMap<>();
    //        //		Map<String, String> unsupportedMap = new HashMap<>();
    //        //		Set<String> undefinedSet = new HashSet<>();
    //        //		if (!checkPropertiesModificationCompatibility(tc, requiredMap, unsupportedMap, undefinedSet))
    //        //			return false;
    //        //		return true;
    //    }

    // void setUpdatedToolChain(ToolChain tch) {
    // tch.copyNonoverriddenSettings(toolChain);
    // toolChain = tch;
    // tch.updateParentFolderInfo(this);
    // }

    //    private String[] getConflictingInputExts(ITool tool1, ITool tool2) {
    //        IProject project = getParent().getOwner().getProject();
    //        String ext1[] = ((Tool) tool1).getAllInputExtensions(project);
    //        String ext2[] = ((Tool) tool2).getAllInputExtensions(project);
    //        Set<String> set1 = new HashSet<>(Arrays.asList(ext1));
    //        Set<String> result = new HashSet<>();
    //        for (String e : ext2) {
    //            if (set1.remove(e))
    //                result.add(e);
    //        }
    //        return result.toArray(new String[result.size()]);
    //    }

}

///*
//* TODO public FolderInfo(FolderInfo base, IPath path, String id, String name) {
//* super(base, path, id, name); }
//*/
//public FolderInfo(IConfiguration parent, IPath path, String id, String name, boolean isExtensionElement) {
//super(parent, path, id, name);
//
//this.isExtensionElement = isExtensionElement;
////if (!isExtensionElement)
////  setResourceData(new BuildFolderData(this));
//
//}

//public FolderInfo(IConfiguration cfg, FolderInfo cloneInfo, String id, Map<IPath, Map<String, String>> superIdMap,
//  boolean cloneChildren) {
//super(cfg, cloneInfo, id);
//
//isExtensionElement = cfg.isExtensionElement();
//if (!isExtensionElement)
//  setResourceData(new BuildFolderData(this));
//
//String subName;
//if (!cloneInfo.isExtensionElement)
//  cloneChildren = true;
//
//boolean copyIds = cloneChildren && id.equals(cloneInfo.id);
//
//IToolChain cloneToolChain = cloneInfo.getToolChain();
//IToolChain extToolChain = ManagedBuildManager.getExtensionToolChain(cloneToolChain);
//if (extToolChain == null)
//  extToolChain = cloneToolChain;
//
//subName = cloneToolChain.getName();
//
//if (cloneChildren) {
//  String subId = copyIds ? cloneToolChain.getId()
//          : ManagedBuildManager.calculateChildId(extToolChain.getId(), null);
//  toolChain = new ToolChain(this, subId, subName, superIdMap, (ToolChain) cloneToolChain);
//
//} else {
//  // Add a tool-chain element that specifies as its superClass the
//  // tool-chain that is the child of the configuration.
//  String subId = ManagedBuildManager.calculateChildId(extToolChain.getId(), null);
//  IToolChain newChain = createToolChain(extToolChain, subId, extToolChain.getName(), false);
//
//  // For each option/option category child of the tool-chain that is
//  // the child of the selected configuration element, create an option/
//  // option category child of the cloned configuration's tool-chain element
//  // that specifies the original tool element as its superClass.
//  newChain.createOptions(extToolChain);
//
//  // For each tool element child of the tool-chain that is the child of
//  // the selected configuration element, create a tool element child of
//  // the cloned configuration's tool-chain element that specifies the
//  // original tool element as its superClass.
//  ITool[] tools = extToolChain.getTools();
//  for (ITool tool : tools) {
//      Tool toolChild = (Tool) tool;
//      subId = ManagedBuildManager.calculateChildId(toolChild.getId(), null);
//      newChain.createTool(toolChild, subId, toolChild.getName(), false);
//  }
//
//  ITargetPlatform tpBase = cloneInfo.getToolChain().getTargetPlatform();
//  ITargetPlatform extTp = tpBase;
//  for (; extTp != null && !extTp.isExtensionElement(); extTp = extTp.getSuperClass()) {
//      // empty body, loop is to find extension element only
//  }
//
//  TargetPlatform tp;
//  if (extTp != null) {
//      int nnn = ManagedBuildManager.getRandomNumber();
//      subId = copyIds ? tpBase.getId() : extTp.getId() + "." + nnn; //$NON-NLS-1$
//      tp = new TargetPlatform(newChain, subId, tpBase.getName(), (TargetPlatform) tpBase);
//  } else {
//      subId = copyIds ? tpBase.getId() : ManagedBuildManager.calculateChildId(getId(), null);
//      subName = tpBase != null ? tpBase.getName() : ""; //$NON-NLS-1$
//      tp = new TargetPlatform((ToolChain) newChain, null, subId, subName, false);
//  }
//
//  ((ToolChain) newChain).setTargetPlatform(tp);
//}
//
//if (isRoot())
//  containsDiscoveredScannerInfo = cloneInfo.containsDiscoveredScannerInfo;
//
//if (copyIds) {
//  isDirty = cloneInfo.isDirty;
//  needsRebuild = cloneInfo.needsRebuild;
//}

//}

//public FolderInfo(FolderInfo folderInfo, String id, String resourceName, IPath path) {
//super(folderInfo, path, id, resourceName);
//
////        isExtensionElement = folderInfo.isExtensionElement();
////        if (!isExtensionElement)
//setResourceData(new BuildFolderData(this));
//
//if (folderInfo.getParent() != null)
//  setManagedBuildRevision(folderInfo.getParent().getManagedBuildRevision());
//
//IToolChain parTc = folderInfo.getToolChain();
//IToolChain extTc = ManagedBuildManager.getExtensionToolChain(parTc);
//if (extTc == null)
//  extTc = parTc;
//
//String tcId = ManagedBuildManager.calculateChildId(extTc.getId(), null);
//createToolChain(extTc, tcId, parTc.getName(), false);
//
//toolChain.createOptions(parTc);
//
//ITool tools[] = parTc.getTools();
//String subId = ""; //$NON-NLS-1$
//for (ITool tool : tools) {
//  ITool extTool = null;//TOFIX JABA ManagedBuildManager.getExtensionTool(tool);
//  if (extTool == null)
//      extTool = tool;
//
//  subId = ManagedBuildManager.calculateChildId(extTool.getId(), null);
//  toolChain.createTool(tool, subId, tool.getName(), false);
//}
//}
