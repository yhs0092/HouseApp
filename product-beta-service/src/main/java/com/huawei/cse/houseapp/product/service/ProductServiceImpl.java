package com.huawei.cse.houseapp.product.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.huawei.cse.houseapp.product.api.ProductInfo;
import com.huawei.cse.houseapp.product.api.ProductService;
import com.huawei.cse.houseapp.product.dao.ProductMapper;
import com.huawei.paas.cse.tcc.annotation.TccTransaction;
import com.netflix.config.DynamicPropertyFactory;

import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.swagger.invocation.exception.InvocationException;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;

@RestSchema(schemaId = "product")
@RequestMapping(path = "/")
public class ProductServiceImpl implements ProductService {
    private AtomicLong db = new AtomicLong(0);

    private static AtomicLong reqCount = new AtomicLong(0);

    private static AtomicLong lastStatTime = new AtomicLong(System.currentTimeMillis());

    static {
        new Thread(() -> {
            while (true) {
                reqCount.compareAndSet(reqCount.get(), 0L);
                lastStatTime.compareAndSet(lastStatTime.get(), System.currentTimeMillis());
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // private ProductMapper productMapper = new MockedProductMapper(); //鍐呭瓨娴嬭瘯
    @Inject
    private ProductMapper productMapper;

    @Inject
    PlatformTransactionManager txManager;

        @GetMapping(path = "cpu")
        public long cpuExtensive(@RequestParam(name = "base") long base) {
                faultInjection();
                if (base < 0) {
                        throw new InvocationException(400, "", "bad param");
                }
                long result = base;
                long next = base - 1;
                while (next > 0) {
                        result = Math.max(next * result, result);
                        next = next - 1;
                }
                return result;
        }

        @GetMapping(path = "mem")
        public long memExtensive(@RequestParam(name = "base") int base) {
                faultInjection();
                if (base < 0) {
                        throw new InvocationException(400, "", "bad param");
                }
                int amout = base * 1024;
                String[] ss = new String[amout];
                for(int i=0; i<ss.length; i++) {
                        ss[i] = new String("i" + i);
                }
                return ss.length;
        }

        private void faultInjection() {
                int delay = DynamicPropertyFactory.getInstance().getIntProperty("cse.test.fault.delay", 0).get();
                if(delay > 1) {
                        try {
                                Thread.sleep(delay);
                        } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                }
                int exception = DynamicPropertyFactory.getInstance().getIntProperty("cse.test.fault.exception", 0).get();
                if(exception > 1) {
                        throw new InvocationException(500, "", "fault injected bad request");
                }
        }

    @Override
    @GetMapping(path = "searchAll")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", dataType = "integer", format = "int32", paramType = "query")})
    public List<ProductInfo> searchAll(@RequestParam(name = "userId") int userId) {
        faultInjection();

        double qps = reqCount.incrementAndGet() * 1000.0d / (System.currentTimeMillis() - lastStatTime.get());
        int configQps = DynamicPropertyFactory.getInstance().getIntProperty("cse.test.product.qps", 10).get();
        if (qps > configQps) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long sleep = DynamicPropertyFactory.getInstance().getLongProperty("cse.test.product.wait", -1).get();
        if (sleep > 0) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        List<ProductInfo> products = productMapper.getAllProducts();
        int reservedCount = 0;
        for (ProductInfo p : products) {
        	if (p.isSold()) {
        		reservedCount ++;
        	}
        }
        
        ProductInfo stat = new ProductInfo();
        stat.setId(1000000);
        stat.setReservedUserId(reservedCount);
        
        products.add(stat);
        return products;
    }

    @Override
    @GetMapping(path = "searchAllForCustomer")
    public List<ProductInfo> searchAllForCustomer() {
        return productMapper.getAllProducts();
    }

    // 瀹為檯鏄噸缃暟鎹帴鍙ｏ紝涓嶆敼鍚嶅瓧浜嗐��
    @Override
    @PostMapping(path = "login")
    public long login(@RequestParam(name = "username") String username,
            @RequestParam(name = "password") String password) {
        // 浣跨敤娴嬭瘯璐﹀彿鐧婚檰锛岀櫥闄嗘垚鍔熷垎閰嶅敮涓�鐨勯�夋埧璐﹀彿. 杩欓噷涓昏鏄负浜嗗苟鍙戝拰鎬ц兘娴嬭瘯鏂逛究锛屽疄闄呬笟鍔″満鏅渶瑕佹寜鐓ц姹傝璁°��
        if ("test".equals(username) && "test".equals(password)) {
            productMapper.clear();

            for (int i = 1; i <= 100; i++) {
                ProductInfo info = new ProductInfo();
                info.setId(i);
                info.setPrice(1000000);
                if (i <= 9) {
                    info.setProductName("product0" + i);
                } else {
                    info.setProductName("product" + i);
                }
                info.setReserved(false);
                info.setReservedUserId(-1);
                info.setSold(false);
                productMapper.createProduct(info);
            }
            return 1L;
        } else {
            return -1;
        }
    }

    @Override
    @PostMapping(path = "buyWithoutTransaction")
    @ApiResponse(code = 400, response = String.class, message = "")
    public boolean buyWithoutTransaction(@RequestParam(name = "productId") long productId,
            @RequestParam(name = "userId") long userId,
            @RequestParam(name = "price") double price) {
        ProductInfo info = productMapper.getProductInfo(productId);
        if (info == null) {
            throw new InvocationException(400, "", "product id not valid");
        }
        if (price != info.getPrice()) {
            throw new InvocationException(400, "", "product price not valid");
        }
        if (info.isSold()) {
            throw new InvocationException(400, "", "product already sold");
        }
        info.setSold(true);
        info.setReservedUserId(userId);
        productMapper.updateProductInfo(info);
        return true;
    }

    @Override
    @PostMapping(path = "buy2pc")
    @ApiResponse(code = 400, response = String.class, message = "")
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public boolean buyWithTransaction2pc(@RequestParam(name = "productId") long productId,
            @RequestParam(name = "userId") long userId,
            @RequestParam(name = "price") double price) {
        ProductInfo info = productMapper.getProductInfo(productId);
        if (info == null) {
            throw new InvocationException(400, "", "product id not valid");
        }
        if (price != info.getPrice()) {
            throw new InvocationException(400, "", "product price not valid");
        }
        if (info.isSold()) {
            throw new InvocationException(400, "", "product already sold");
        }
        info.setSold(true);
        info.setReservedUserId(userId);
        productMapper.updateProductInfo(info);
        return true;
    }

    @Override
    @TccTransaction(cancelMethod = "cancelBuy", confirmMethod = "confirmBuy")
    @PostMapping(path = "buy")
    @ApiResponse(code = 400, response = String.class, message = "")
    public boolean buyWithTransaction(@RequestParam(name = "productId") long productId,
            @RequestParam(name = "userId") long userId,
            @RequestParam(name = "price") double price) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        TransactionStatus status = txManager.getTransaction(def);

        try {
            // 浣跨敤閿佹満鍒讹紝闃叉澶氱嚎绋嬪苟鍙戝浜巔roduct鐨勫悓鏃舵姠璐�� getProductInfo浣跨敤浜唂or
            // update锛屼簨鍔′細鍔犻攣锛屼笉浼氬苟鍙戙�傝繖閲屼娇鐢ㄤ簡spring浜嬪姟銆�
            ProductInfo info = productMapper.getProductInfo(productId);
            if (info == null) {
                throw new InvocationException(400, "", "product id not valid");
            }
            if (price != info.getPrice()) {
                txManager.commit(status);
                throw new InvocationException(400, "", "product price not valid");
            }
            if (info.isReserved() || info.isSold()) {
                txManager.commit(status);
                return false;
            }
            info.setReserved(true);
            info.setReservedUserId(userId);
            productMapper.updateProductInfo(info);
            txManager.commit(status);
            return true;
        } catch (Exception e) {
            txManager.rollback(status);
            throw e;
        }
    }

    @ApiOperation(hidden = true, value = "")
    public void cancelBuy(long productId, long userId, double price) {
        ProductInfo info = productMapper.getProductInfo(productId);
        if (info == null) {
            return;
        }
        if (info.isReserved() && info.getReservedUserId() == userId) {
            info.setReserved(false);
            productMapper.updateProductInfo(info);
        }
    }

    @ApiOperation(hidden = true, value = "")
    public void confirmBuy(long productId, long userId, double price) {
        ProductInfo info = productMapper.getProductInfo(productId);
        if (info == null) {
            return;
        }
        if (info.isReserved()) {
            info.setReserved(false);
            info.setSold(true);
            productMapper.updateProductInfo(info);
        }
    }

    @Override
    @PostMapping(path = "add")
    public void addProduct(double price) {
        ProductInfo info = new ProductInfo();
        long i = db.incrementAndGet();
        info.setId(i);
        info.setPrice(1000000);
        info.setProductName("product" + i);
        info.setReserved(false);
        info.setReservedUserId(-1);
        info.setSold(false);
        productMapper.createProduct(info);

    }

    @Override
    @GetMapping(path = "queryReduced")
    public double queryReduced() {
        Double reduced = productMapper.queryReduced();
        if (reduced == null) {
            return 0D;
        } else {
            return reduced;
        }
    }
}
