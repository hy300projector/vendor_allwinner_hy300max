#
# SPDX-FileCopyrightText: The Android Open Source Project
# SPDX-License-Identifier: Apache-2.0
#

# Extracted factory blobs only: firmware, video/codec libraries, DSP and TV
# configuration. Everything we reverse-engineered is built from source in
# device/allwinner/hy300max; kernel modules come from device prebuilts.
# Generated from device/allwinner/hy300max/proprietary-files.txt by
# device/allwinner/hy300max/extract-files.py.
$(call inherit-product, vendor/allwinner/hy300max/hy300max-vendor-blobs.mk)
