# Copyright 2017 Esri
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# For additional information, contact:
# Environmental Systems Research Institute, Inc.
# Attn: Contracts Dept
# 380 New York Street
# Redlands, California, USA 92373
#
# email: contracts@esri.com
#
#

# SUMMARY:
# Creates an overlapping set of polygons attributed with Depth level and EMU cluster
# which have been derived from the Ecological Marine Units' set of 52+ million points.
#
# NOTE:
# For quicker processing times, ensure an attribute index exists for the 'depth_lvl' field
# on the input set of points.
#
# Requires Python 2.7+
#
# SAMPLE USAGE:
# python ProcessEMUs.py -w "{workspace}" -o -b "{baseFC directory}" -n "{newMergedFC}"

import arcpy
import sys
import argparse
import time
import datetime as dt


def generate_level_raster(new_selection_fc, depth_lvl):
    """
    Pass in the selected pionts from the current depth level
    and generate a new EMU raster from them.

    Args:
        new_selection_fc: Feature class comprised only of points from the defined depth level.
        depth_lvl: Depth level defined within the current loop.
    Returns:
        emu_raster: Raster whose cell values reflect the EMU cluster.
    """
    emu_raster = "EMURaster" + str(depth_lvl)
    arcpy.PointToRaster_conversion(
        in_features=new_selection_fc,
        value_field="Cluster37",
        out_rasterdataset=emu_raster,
        cellsize=0.25)
    arcpy.Delete_management(new_selection_fc)
    return emu_raster


def extract_level_polygons(emu_raster, depth_lvl):
    """
    Extract each of the unique values present in the raster
    into a new polygon feature class.

    Args:
        emu_raster: Raster whose cell values match the EMU cluster.
        depth_lvl: Depth level defined within the current loop.
    Returns:
        emu_polygons: Vectorized derivative of the input EMU raster.
    """
    emu_polygons = "EMUPolygons_" + str(depth_lvl)
    arcpy.RasterToPolygon_conversion(
        in_raster=emu_raster,
        out_polygon_features=emu_polygons,
        simplify="NO_SIMPLIFY",
        raster_field="Value")
    arcpy.Delete_management(emu_raster)
    return emu_polygons


def dissolve_polygons(emu_polygons, depth_lvl):
    """
    Dissolve all like EMU values together into one feature.

    Args:
        emu_polygons: Vectorized derivative of the EMU raster.
        depth_lvl: Depth level defined within the current loop.
    Returns:
        emu_dissolved_polys: Same as the input EMU polygons, however,
        non-adjacent features sharing same attributes become a single record.
     """
    emu_dissolved_polys = "EMUDissolvedPolys_" + str(depth_lvl)
    arcpy.Dissolve_management(
        in_features=emu_polygons,
        out_feature_class=emu_dissolved_polys,
        dissolve_field="gridcode",
        multi_part="MULTI_PART")
    arcpy.Delete_management(emu_polygons)
    return emu_dissolved_polys


def update_field_schema(emu_dissolved_polys, depth_lvl):
    """
    Keep the field schema organized. Transfer existing values
    into more easily interpreted 'EMU' and 'Depth' fields.

    Args:
        emu_dissolved_polys: Multipart EMU polygons from the current depth level.
        depth_lvl: Depth level defined within the current loop.
    """
    arcpy.AddField_management(
        in_table=emu_dissolved_polys,
        field_name="Depth",
        field_type="SHORT")
    arcpy.AddField_management(
        in_table=emu_dissolved_polys,
        field_name="EMU",
        field_type="SHORT")
    arcpy.CalculateField_management(
        in_table=emu_dissolved_polys,
        field="Depth",
        expression=depth_lvl)
    arcpy.CalculateField_management(
        in_table=emu_dissolved_polys,
        field="EMU",
        expression="[gridcode]")
    arcpy.DeleteField_management(
        in_table=emu_dissolved_polys,
        drop_field="gridcode")


def merge_polygons(new_poly_fc, emu_dissolved_polys, depth_lvl):
    """
    Merges all feature from the current depth level into
    a single, output feature class and ensures that the
    output is freshly created each time the script runs.

    Args:
        new_poly_fc: New polygon feature class to which the loop's output will be appended.
        emu_dissolved_polys: Mulitpart EMU polygons attributed with Depth and EMU.
        depth_lvl: Depth level defined within the current loop.
    """
    if depth_lvl == 1:
        if arcpy.Exists(new_poly_fc):
            arcpy.Delete_management(new_poly_fc)
        arcpy.CreateFeatureclass_management(
            out_path=arcpy.env.workspace,
            out_name=new_poly_fc,
            geometry_type="POLYGON",
            template=emu_dissolved_polys,
            spatial_reference=emu_dissolved_polys)
    elif not arcpy.Exists(new_poly_fc):
        # condition shouldn't occur, but cancel the script if so
        print sys.exit("Output feature class does not exist!")
    arcpy.Append_management(
        inputs=emu_dissolved_polys,
        target=new_poly_fc)
    arcpy.Delete_management(emu_dissolved_polys)


def generate_emu_polygons(new_selection_fc, depth_lvl, new_poly_fc):
    """
    Calls each of the previously-defined methods in order.
    There is conservative logic to ensure the script fails
    gracefully in the event that the output is not created
    by the previous function.

    Args:
        new_selection_fc: Feature class comprised only of points from the defined depth level.
        depth_lvl: Depth level defined within the current loop.
        new_poly_fc: New polygon feature class to which the loop's output will be appended.
    """
    # convert points to raster
    if arcpy.Exists(new_selection_fc):
        print "Rasterizing points..."
        emu_raster = generate_level_raster(new_selection_fc, depth_lvl)

        # convert raster to polygons (don't simplify)
        if arcpy.Exists(emu_raster):
            print "Converting raster to polygons..."
            emu_polygons = extract_level_polygons(emu_raster, depth_lvl)

            # dissolve to form only one polygon feature per EMU cluster
            if arcpy.Exists(emu_polygons):
                print "Dissolving to form multipart features..."
                emu_dissolved_polys = dissolve_polygons(emu_polygons, depth_lvl)

                # update field schema
                if arcpy.Exists(emu_dissolved_polys):
                    print "Updating field schema..."
                    update_field_schema(emu_dissolved_polys, depth_lvl)

                    # create and/or append to merged feature class
                    # if on first loop, delete and recreate merged feature class
                    print "Merging level {} data...".format(depth_lvl)
                    merge_polygons(new_poly_fc, emu_dissolved_polys, depth_lvl)


def process_emus_by_level(base_point_fc, new_poly_fc):
    """
    Prepares the script for running once per each depth level
    and catpures some timestamps for script progression reporting.

    Args:
        base_point_fc: Input set of 52+ million EMU points.
        new_poly_fc: New polygon feature class to which the loop's output will be appended.
    """
    beginning = time.time()

    # generate one EMU polygon layer per depth level
    for depth_lvl in range(1, 101):
        where_clause = "depth_lvl = {0}".format(str(depth_lvl))
        start = time.time()

        # select points from next incremented depth level
        new_selection_fc = "Depth_{0}".format(str(depth_lvl))
        print "\nSelecting features for level {}...".format(str(depth_lvl))
        arcpy.Select_analysis(base_point_fc, new_selection_fc, where_clause)

        try:
            generate_emu_polygons(new_selection_fc, depth_lvl, new_poly_fc)

        except Exception as e:
            print "Exception: {0}".format(str(sys.exc_info()[0]))
            print arcpy.GetMessages()
            print sys.exit("Script terminated.")

        end = time.time()

        # let us know how quickly things are progressing
        print "Depth Level = {0} loop time took {1} seconds".format(str(depth_lvl), str(end - start))
        print "Total time elapsed: {0}".format(str(dt.timedelta(seconds=(end - beginning))))


def main():
    """
    Executes the script in its entirety.

    """
    parser = argparse.ArgumentParser(
        description='This program processes EMU points into attributed polygons representing global ocean coverage.',
        epilog='',
        add_help=True,
        argument_default=None,  # Global argument default
        usage=__doc__)
    parser.add_argument('-w', '--workspace', action='store', dest='workspace_gdb', required=True,
                        help='The default geodatabase to which the intermediate outputs should be written.')
    parser.add_argument('-o', '--overwrite_output', action='store_true', required=True, dest='overwrite_output',
                        default="", help='Overwrite output?')
    parser.add_argument('-b', '--base_fc', action='store', dest='base_fc', required=True, default="",
                        help='The location of the input set of EMU points.')
    parser.add_argument('-n', '--new_poly_fc', action='store', dest='new_poly_fc', required=True, default="",
                        help='The name that should be given to the output set of derivative polygons.')

    arguments = parser.parse_args()

    arcpy.env.workspace = arguments.workspace_gdb
    arcpy.env.overwriteOutput = arguments.overwrite_output
    base_point_fc = arguments.base_fc
    new_poly_fc = arguments.new_poly_fc

    print "Start time: {0}".format(str(dt.datetime.now()))

    process_emus_by_level(base_point_fc, new_poly_fc)

    print "End time: {0}".format(str(dt.datetime.now()))


if __name__ == "__main__":
    main()