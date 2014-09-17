/*
 * SkinSwitch - OnSkinLoadingListener
 * Copyright (C) 2014-2014  Baptiste Candellier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.outadev.skinswitch.skinlist;

/**
 * Defines an activity that has a progess indicator.
 * Created by outadoc on 19/07/14.
 */
public interface OnSkinLoadingListener {

	/**
	 * Displays a loading state.
	 *
	 * @param loading true if loading, false if done.
	 */
	public void setLoading(boolean loading);

}
