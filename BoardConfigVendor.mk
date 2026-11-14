#
# SPDX-License-Identifier: Apache-2.0
#

# The device uses dynamic partitions with separate vendor, product and DLKM
# images.  These outputs are what the stock fstab and the generated super image
# expect; without them AOSP installs vendor modules into system/vendor.
TARGET_COPY_OUT_VENDOR := vendor
TARGET_COPY_OUT_PRODUCT := product
TARGET_COPY_OUT_SYSTEM_DLKM := system_dlkm
TARGET_COPY_OUT_VENDOR_DLKM := vendor_dlkm

# Keep system_ext in the system image; this device has no system_ext partition
# in its super partition group.
TARGET_COPY_OUT_SYSTEM_EXT := system/system_ext

# Filesystems from the stock fstab.
BOARD_SYSTEMIMAGE_FILE_SYSTEM_TYPE := ext4
BOARD_VENDORIMAGE_FILE_SYSTEM_TYPE := ext4
BOARD_PRODUCTIMAGE_FILE_SYSTEM_TYPE := ext4
BOARD_SYSTEM_DLKMIMAGE_FILE_SYSTEM_TYPE := ext4
BOARD_VENDOR_DLKMIMAGE_FILE_SYSTEM_TYPE := erofs

# The vendor_dlkm modules are built out-of-tree against linux-5.15.167 and are
# packed by build_vendor_dlkm.sh.  The stock partition is a 4,952,064-byte
# compressed erofs image, so keep using that image format here instead of
# letting AOSP build an uncompressed vendor_dlkm image.
BOARD_PREBUILT_VENDOR_DLKMIMAGE := vendor/allwinner/hy300max/prebuilts/vendor_dlkm_a.img

# The vendor partition is built from this tree: AOSP modules plus the blobs in
# vendor/allwinner/hy300max/proprietary. The factory vendor image is never
# shipped - it is the partition that carried the BadBox 2.0 preinstall, and it
# also bypassed every source-built HAL in this device tree.

