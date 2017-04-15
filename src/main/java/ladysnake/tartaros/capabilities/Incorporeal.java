package ladysnake.tartaros.capabilities;

public class Incorporeal implements IIncorporeal {
	
	private boolean incorporeal;

	@Override
	public void setIncorporeal(boolean enable) {
		incorporeal = enable;
		
	}

	@Override
	public boolean isIncorporeal() {
		return incorporeal;
	}

}
