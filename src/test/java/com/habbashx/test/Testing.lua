local ffi = require("ffi")

ffi.cdef [[

  bool setCursorPos(int x, int y)

]]

local user32 = ffi.load("user32")

user32.setCursorPos(500, 300)