local Testing = {}
Testing.__index = Testing

function Testing.new()
    local self = setmetatable({}, Testing)
    self.name = name
    self.age = age

    return self
end

function Testing:sayHello()
    print("Hi i`m, " .. self.name)
end

local t = Testing.new("Abood", 42)
t.sayHello()
