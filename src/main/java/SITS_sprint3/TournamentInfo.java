package SITS_sprint3;

public class TournamentInfo
{
    private final int id;
    private final String name;
    private final boolean active;
    private final boolean registrationOpen;

    public TournamentInfo(int id, String name, boolean active, boolean registrationOpen)
    {
        this.id = id;
        this.name = name;
        this.active = active;
        this.registrationOpen = registrationOpen;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public boolean isActive()
    {
        return active;
    }

    public boolean isRegistrationOpen()
    {
        return registrationOpen;
    }

    public String getStatusText()
    {
        if (registrationOpen)
        {
            return "Registration";
        }

        if (active)
        {
            return "Active";
        }

        return "Closed";
    }

    @Override
    public String toString()
    {
        return "Tournament " + id + " - " + name + " [" + getStatusText() + "]";
    }
}