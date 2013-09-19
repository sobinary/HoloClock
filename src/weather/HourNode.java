package weather;

import java.io.Serializable;

public class HourNode implements Serializable 
{
	private static final long serialVersionUID = -2022494482956075128L;
	public int day, hour;
	public String cond;
	public int temp;
	
	public void convert()
	{
		if(cond.equals("mostlycloudy")) cond = "cloudy";
		else if(cond.equals("chancesnow")) cond = "snow";
		else if(cond.equals("chancerain")) cond = "rain";
		else if(cond.equals("chancetstorms")) cond = "tstorms";
	}
	
	@Override
	public String toString()
	{
		return "{"+cond+";"+day+";"+hour+"}";
	}
}

