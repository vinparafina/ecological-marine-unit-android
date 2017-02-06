import arcpy
import sys
import time
import datetime as dt

def generate_level_raster(new_selection_fc, depth_lvl):
    emu_raster = "EMURaster" + str(depth_lvl)
    arcpy.PointToRaster_conversion(
        in_features=new_selection_fc,
        value_field="Cluster37",
        out_rasterdataset=emu_raster,
        cellsize=0.25)
    arcpy.Delete_management(new_selection_fc)
    return emu_raster

def extract_level_polygons(emu_raster, depth_lvl):
    emu_polygons = "EMUPolygons_" + str(depth_lvl)
    arcpy.RasterToPolygon_conversion(
        in_raster=emu_raster,
        out_polygon_features=emu_polygons,
        simplify="NO_SIMPLIFY",
        raster_field="Value")
    arcpy.Delete_management(emu_raster)
    return emu_polygons

def dissolve_polygons(emu_polygons, depth_lvl):
    emu_dissolved_polys = "EMUDissolvedPolys_" + str(depth_lvl)
    arcpy.Dissolve_management(
        in_features = emu_polygons,
        out_feature_class = emu_dissolved_polys,
        dissolve_field = "gridcode",
        multi_part = "MULTI_PART")
    arcpy.Delete_management(emu_polygons)
    return emu_dissolved_polys

def update_field_schema(emu_dissolved_polys, depth_lvl):
    arcpy.AddField_management(
        in_table = emu_dissolved_polys,
        field_name = "Depth",
        field_type = "SHORT")
    arcpy.AddField_management(
        in_table = emu_dissolved_polys,
        field_name = "EMU",
        field_type = "SHORT")
    arcpy.CalculateField_management(
        in_table = emu_dissolved_polys,
        field = "Depth",
        expression = depth_lvl)
    arcpy.CalculateField_management(
        in_table = emu_dissolved_polys,
        field = "EMU",
        expression = "[gridcode]")
    arcpy.DeleteField_management(
        in_table = emu_dissolved_polys,
        drop_field = "gridcode")

def merge_polygons(emu_dissolved_polys, depth_lvl):
    if depth_lvl == 1:
        if arcpy.Exists(new_poly_fc):
            arcpy.Delete_management(new_poly_fc)
        arcpy.CreateFeatureclass_management(
            out_path = arcpy.env.workspace,
            out_name = new_poly_fc,
            geometry_type = "POLYGON",
            template = emu_dissolved_polys,
            spatial_reference = emu_dissolved_polys)
    elif not arcpy.Exists(new_poly_fc):
        # condition shouldn't exist, but cancel the script if so
        print sys.exit("Output feature class does not exist!")
    arcpy.Append_management(
        inputs = emu_dissolved_polys,
        target = new_poly_fc)
    arcpy.Delete_management(emu_dissolved_polys)

def generate_emu_polygons(new_selection_fc, depth_lvl):
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
                    merge_polygons(emu_dissolved_polys, depth_lvl)                   

def process_emus_by_level(base_point_fc, new_poly_fc):
    beginning = time.time()
    
    # generate one EMU polygon layer per depth level
    for depth_lvl in range(1, 101):
        where_clause = "depth_lvl = {0}".format(str(depth_lvl))
        start = time.time()

        # select points from next incremented depth level
        new_selection_fc = "Depth_{0}".format(str(depth_lvl))
        print "Where Clause: {0}, new_selection_fc: {1}".format(where_clause, new_selection_fc)
        print "Selecting features..."
        arcpy.Select_analysis(base_point_fc, new_selection_fc, where_clause)

        try:
            process_emus_by_level(new_selection_fc, depth_lvl)

        except Exception as e:
            print "Exception: {0}".format(str(sys.exc_info()[0]))
            print arcpy.GetMessages()
            print sys.exit("Script terminated.")
        
        end = time.time()

        # let us know how quickly things are progressing
        print "Depth Level = {0} loop time took {1} seconds".format(str(depth_lvl), str(end-start))
        print "Total time elapsed: {0}\n".format(str(dt.timedelta(seconds=(end-beginning))))

def main():
    # set default geodatabase and location of EMU points
    arcpy.env.workspace =                   # like r"C:\Data\EMUData.gdb"
    arcpy.env.overwriteOutput = True
    base_point_fc =                         # like r"C:\Data\EMUGlobal.gdb\EMUMaster"
    new_poly_fc =                           # like "Global_EMU_Polygons"

    print "Start time: {0}".format(str(dt.datetime.now()))

    process_emus_by_level(base_point_fc, new_poly_fc):

    print "End time: {0}".format(str(dt.datetime.now()))

if __name__ == "__main__":
    main()