-- 优惠券id
local vocherId = ARGV[1]
-- 用户id
local userId = ARGV[2]

-- 2. 数据key
-- 库存key
local stockKey = 'seckill:stock:' .. vocherId
-- 订单key
local orderKey = 'seckill:order:' .. vocherId

-- 脚本业务
-- 判断库存是否充足 get stockKey
if (tonumber(redis.call('get',stockKey)) <= 0) then
    -- 库存不足
    return 1
end

-- 判断用户是否下单 sismember orderKey userId
if (redis.call('sismember', orderKey,userId) == 1) then
    -- 存在说明重复下单
    return 2
end
-- 扣库存 incrby stockKey -1
redis.call('incrby', stockKey, -1)
-- 下单 sadd orderKey userId
redis.call('sadd', orderKey, userId)
return 0

