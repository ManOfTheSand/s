package com.sandcore.mmo.util;

import com.sandcore.mmo.manager.ClassManager;
import com.sandcore.mmo.manager.XPManager;
import com.sandcore.mmo.manager.CurrencyManager;
import com.sandcore.mmo.manager.PartyManager;

public class ServiceRegistry {
    
    private static ClassManager classManager;
    private static XPManager xpManager;
    private static CurrencyManager currencyManager;
    private static PartyManager partyManager;
    
    public static void registerServices() {
        classManager = new ClassManager();
        xpManager = new XPManager();
        currencyManager = new CurrencyManager();
        partyManager = new PartyManager();
    }
    
    public static ClassManager getClassManager() {
        return classManager;
    }
    
    public static XPManager getXPManager() {
        return xpManager;
    }
    
    public static CurrencyManager getCurrencyManager() {
        return currencyManager;
    }
    
    public static PartyManager getPartyManager() {
        return partyManager;
    }
} 