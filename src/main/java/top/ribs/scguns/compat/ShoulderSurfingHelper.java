package top.ribs.scguns.compat;

import com.github.exopandora.shouldersurfing.api.model.Perspective;
import top.ribs.scguns.ScorchedGuns;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Author: NineZero
 */
@SuppressWarnings("unused")
public class ShoulderSurfingHelper
{
    private static boolean disable1 = false;
    private static boolean disable2 = false;
    private static Method getShoulderInstance;
    private static Method isShoulderSurfing;
    private static Method changePerspective;

    public static boolean isShoulderSurfing()
    {
        if(!ScorchedGuns.shoulderSurfingLoaded)
            return false;
        
        if (!disable1)
            try
            {
                init();
                Object object = getShoulderInstance.invoke(null);
                return (boolean) isShoulderSurfing.invoke(object);
            }
            catch(InvocationTargetException | IllegalAccessException | IllegalArgumentException | NullPointerException e)
            {
                ScorchedGuns.LOGGER.error("Shoulder Surfing helper error with method isShoulderSurfing!");
                e.printStackTrace();
                disable1 = true;
            }
        else
        if (!disable2)
            try
            {
                init();
                Object object = getShoulderInstance.invoke(null);
                return (boolean) isShoulderSurfing.invoke(object);
            }
            catch(InvocationTargetException | IllegalAccessException | IllegalArgumentException | NullPointerException e)
            {
                ScorchedGuns.LOGGER.error("Shoulder Surfing helper error with method isShoulderSurfing!");
                e.printStackTrace();
                disable2 = true;
            }
        return false;
    }

    public static void changePerspective(String perspective)
    {
        if(!ScorchedGuns.shoulderSurfingLoaded)
            return;
        
        if (!disable1)
            try
            {
                init();
                Perspective pov = Perspective.SHOULDER_SURFING;
                if (perspective.toUpperCase().equals("FIRST_PERSON")) pov = Perspective.FIRST_PERSON;
                else if (perspective.toUpperCase().equals("THIRD_PERSON_BACK")) pov = Perspective.THIRD_PERSON_BACK;
                else if (perspective.toUpperCase().equals("THIRD_PERSON_FRONT")) pov = Perspective.THIRD_PERSON_FRONT;
                Object object = getShoulderInstance.invoke(null);
                changePerspective.invoke(object, pov);
            }
            catch(InvocationTargetException | IllegalAccessException | NullPointerException e)
            {
                ScorchedGuns.LOGGER.error("Shoulder Surfing helper error with method changePerspective!");
                e.printStackTrace();
                disable1 = true;
            }
        else
        if (!disable2)
            try
            {
                init();
                Perspective pov = Perspective.SHOULDER_SURFING;
                if (perspective.toUpperCase().equals("FIRST_PERSON")) pov = Perspective.FIRST_PERSON;
                else if (perspective.toUpperCase().equals("THIRD_PERSON_BACK")) pov = Perspective.THIRD_PERSON_BACK;
                else if (perspective.toUpperCase().equals("THIRD_PERSON_FRONT")) pov = Perspective.THIRD_PERSON_FRONT;
                Object object = getShoulderInstance.invoke(null);
                changePerspective.invoke(object, pov);
            }
            catch(InvocationTargetException | IllegalAccessException | NullPointerException e)
            {
                ScorchedGuns.LOGGER.error("Shoulder Surfing helper error with method changePerspective!");
                e.printStackTrace();
                disable2 = true;
            }
    }

    private static void init()
    {
        if(getShoulderInstance == null)
        {
            try
            {
                Class<?> shoulderSurfingImpl = Class.forName("com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl");
                getShoulderInstance = shoulderSurfingImpl.getDeclaredMethod("getInstance");
                isShoulderSurfing = shoulderSurfingImpl.getDeclaredMethod("isShoulderSurfing");
                Class<?>[] pArg = new Class[1];
                pArg[0] = Perspective.class;
                changePerspective = shoulderSurfingImpl.getDeclaredMethod("changePerspective", pArg);
                
            }
            catch(ClassNotFoundException | NoSuchMethodException | NullPointerException ignored)
            {
            	if (getShoulderInstance!=null)
            	{
                    ScorchedGuns.LOGGER.error("Shoulder Surfing helper failed to load!");
            		ignored.printStackTrace();
            	}
            	disable1 = true;
            }
            if (disable1)
                try
                {
                    Class<?> shoulderInstance = Class.forName("com.github.exopandora.shouldersurfing.client.ShoulderInstance");
                    getShoulderInstance = shoulderInstance.getDeclaredMethod("getInstance");
                    isShoulderSurfing = shoulderInstance.getDeclaredMethod("doShoulderSurfing");
                    Class<?>[] pArg = new Class[1];
                    pArg[0] = Perspective.class;
                    changePerspective = shoulderInstance.getDeclaredMethod("changePerspective", pArg);
                }
                catch(ClassNotFoundException | NoSuchMethodException | NullPointerException ignored)
                {
                    disable2 = true;
                }
            if (disable1 && disable2)
                ScorchedGuns.LOGGER.info("Shoulder Surfing Reloaded is not installed, proceeding without helper.");
        }
    }
}