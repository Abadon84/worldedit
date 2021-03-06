// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.tools.brushes.Brush;
import com.sk89q.worldedit.tools.brushes.SphereBrush;

/**
 * Builds a shape at the place being looked at.
 * 
 * @author sk89q
 */
public class BrushTool implements TraceTool {
    private Mask mask = null;
    private Brush brush = new SphereBrush();
    private Pattern material = new SingleBlockPattern(new BaseBlock(BlockID.COBBLESTONE));
    private int size = 1;
    private String permission;
    
    /**
     * Construct the tool.
     * 
     * @param permission
     */
    public BrushTool(String permission) {
        this.permission = permission;
    }
    
    /**
     * Checks to see if the player can still be using this tool (considering
     * permissions and such).
     * 
     * @param player
     * @return
     */
    public boolean canUse(LocalPlayer player) {
        return player.hasPermission(permission);
    }
    
    /**
     * Get the filter.
     * 
     * @return the filter
     */
    public Mask getMask() {
        return mask;
    }

    /**
     * Set the block filter used for identifying blocks to replace.
     * 
     * @param filter the filter to set
     */
    public void setMask(Mask filter) {
        this.mask = filter;
    }

    /**
     * Set the brush.
     * 
     * @param brush
     * @param perm 
     */
    public void setBrush(Brush brush, String perm) {
        this.brush = brush;
        this.permission = perm;
    }
    
    /**
     * Get the current brush.
     * 
     * @return
     */
    public Brush getBrush() {
        return brush;
    }
    
    /**
     * Set the material.
     * 
     * @param material
     */
    public void setFill(Pattern material) {
        this.material = material;
    }
    
    /**
     * Get the material.
     * 
     * @return
     */
    public Pattern getMaterial() {
        return material;
    }

    /**
     * Get the set brush size.
     * 
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     * Set the set brush size.
     * 
     * @param size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Perform the action. Should return true to deny the default
     * action.
     * 
     * @param player
     * @param session
     * @return true to deny
     */
    public boolean act(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session) {
        WorldVector target = player.getBlockTrace(500);
        
        if (target == null) {
            player.printError("No block in sight!");
            return true;
        }
        
        BlockBag bag = session.getBlockBag(player);
        
        EditSession editSession;
        
        if (mask == null) {
            editSession = new EditSession(target.getWorld(),
                    session.getBlockChangeLimit(), bag);
        } else {
            editSession = new ReplacingEditSession(target.getWorld(),
                    session.getBlockChangeLimit(), bag, mask);
        }
        
        try {
            brush.build(editSession, target, material, size);
        } catch (MaxChangedBlocksException e) {
            player.printError("Max blocks change limit reached.");
        } finally {
            if (bag != null) {
                bag.flushChanges();
            }
            session.remember(editSession);
        }
        
        return true;
    }

}
