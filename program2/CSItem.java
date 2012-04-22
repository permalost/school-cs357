import java.util.*;

/**
 * An item in the constraints satisfaction grocery bagging problem.
 * 
 * @author Quincy Bowers quincybowers@u.boisestate.edu
 * @version 1.0
 */
public class CSItem implements Comparable<CSItem>
{
    private String name;
    private int size;
    private ArrayList<CSBag> possibleBags;
    private CSBag assignedBag;
    private List<CSItem> constraints;
    
    /**
     * Constructor
     * 
     * @param name the item's name.
     * @param size the size of the item.
     */
    public CSItem(String name, int size, ArrayList<CSBag> bags)
    {
        this.name = name;
        this.size = size;
        constraints = new ArrayList<CSItem>();
        
        this.possibleBags = new ArrayList<CSBag>(bags.size());
        
        for (CSBag bag : bags)
        {
            this.possibleBags.add(bag);
        }
        
        this.assignedBag = null;
    }
    
    /**
     * Get a string representing this object.
     * 
     * @return a string representing this object.
     */
    @Override
    public String toString()
    {
        String str = name + " " + size;
        
        if (this.assignedBag != null)
        {
            str = str.concat(" in " + assignedBag.name());
        }
        
        str = str.concat("\nConstraints: " + getConstraintsString()
                       + "\nBags: " + getBagString());
        
        return str;
    }
    
    /**
     * Name of the item.
     * 
     * @return the name of the item.
     */
    public String name()
    {
        return name;
    }
    
    /**
     * Size of the item.
     * 
     * @return the size of the item.
     */
    public int size() 
    {
        return size;
    }
    
    /**
     * Initialize the constraints for this item.
     * 
     * @param items a list of all of the items in the problem.
     * @param clist a list of the constraints for this item.  The first element
     *        of this list must be a + or minus indicating whether this is a list
     *        of positive or negative constraints.
     */
    public void initializeConstraints(ArrayList<CSItem>items, List<String>clist)
    {
        if (clist.get(0).equals("-"))
        {
            for (int i=0; i<items.size(); ++i)
            {
                if (clist.contains(items.get(i).name()))
                {
                    addConstraint(items.get(i));
                    items.get(i).addConstraint(this);
                }
            }
        }
        else if (clist.get(0).equals("+"))
        {
            for (int i=0; i<items.size(); ++i)
            {
                if (!clist.contains(items.get(i).name()))
                {
                    addConstraint(items.get(i));
                    items.get(i).addConstraint(this);
                }
            }
        }
        else
        {
            System.err.println("Error initializing constraints for " + name + ".");
            System.exit(1);
        }
    }
    
    /**
     * Adds a new negative constraint to this item.
     * 
     * @param item the item that this item can't pack with.
     */
    public void addConstraint(CSItem item)
    {
        if (constraints.contains(item))  return;
        
        constraints.add(item);
    }
    
    /**
     * Get the entire list of constraints as a string.
     * 
     * @return the string representation of the constraints list.
     */
    public String getConstraintsString()
    {
        String str = "";
        
        for (CSItem item : constraints)
        {
            str = str.concat(item.name());
            str = str.concat(" ");
        }
        
        return str;
    }
    
    
    /**
     * Get the entire list of bags that could be assigned as a string.
     * 
     * @return the string representation of the bags list.
     */
    public String getBagString()
    {
        String str = "";
        
        for (CSBag bag : possibleBags)
        {
            str = str.concat(bag.name());
            str = str.concat(" ");
        }
        
        return str;
    }
    /**
     * Check if this item can pack with the specified item.
     * 
     * @param item the item to check.
     * 
     * @return true if the items can pack together; false otherwise.
     */
    public boolean packsWith(CSItem item)
    {
        if (constraints.contains(item))  return false;
        
        return true;
    }
    
    /**
     * 
     * @return
     */
    public List<CSItem> getConstraints()
    {
        return this.constraints;
    }
    
    /**
     * 
     * @param bag
     */
    public void pack(CSBag bag) throws SubtreeUnsolvable
    {
        this.assignedBag = bag;
        
        for (int i=0; i<constraints.size(); ++i)
        {
            constraints.get(i).removeBag(bag);
            if (!constraints.get(i).assigned() && constraints.get(i).numBags() == 0)
            {
                // We have to add this bag back into every item where it was removed.
                for (int j=i; j>=0; --j)
                {
                    constraints.get(j).addBag(bag);
                }
                
                this.assignedBag = null;
                
                throw new SubtreeUnsolvable(
                        "Assignment of " 
                      + bag.name() 
                      + " reduces domain of " 
                      + constraints.get(i).name() 
                      + " to an empty set.");
            }
        }
    }
    
    /**
     * Removes the bag assignment from this item.
     */
    public void unpack()
    {
        if (assignedBag == null) { return; }
        
        for (CSItem item : constraints)
        {
            if (this.assignedBag.canPack(item))
            {
                item.addBag(this.assignedBag);
            }
        }
        
        this.assignedBag = null;
    }
    
    /**
     * Return the item this bag is packed in.
     * 
     * @return the bag this item is packed in.
     */
    public CSBag bag()
    {
        return this.assignedBag;
    }
    
    /**
     * Check if this items has been assigned a bag.
     * 
     * @return true is assigned; false otherwise.
     */
    public boolean assigned()
    {
        if (assignedBag == null)
        {
            return false;
        }
        return true;
    }
    
    /**
     * 
     * @param bag
     */
    public void removeBag(CSBag bag)
    {
        possibleBags.remove(bag);
    }
    
    /**
     * 
     * @param bag
     */
    public void addBag(CSBag bag)
    {
        if (possibleBags.contains(bag)) { return; }
        possibleBags.add(bag);
    }
    
    /**
     * 
     * @param b
     * @return
     */
    public CSBag getBag(int b)
    {
        return possibleBags.get(b);
    }
    
    /**
     * 
     * @return
     */
    public int numBags()
    {
        return possibleBags.size();
    }
    
    /**
     * 
     * @param bag
     * @return
     */
    public boolean hasBag(CSBag bag)
    {
        return possibleBags.contains(bag);
    }
    
    /**
     * 
     * @return
     */
    public int numConstraints()
    {
        return constraints.size();
    }
    
    /**
     * Calculates the degree of the item where degree is the number of 
     * constraints this item is involved in with other unnassigned items.
     * 
     * @return the degree.
     */
    public int degree()
    {
        int degree = 0;
        
        for (CSItem item : constraints)
        {
            if (!item.assigned())
            {
                ++degree;
            }
        }
        
        return degree;
    }

    /**
     * Returns the list of possible bags to assign.
     * 
     * @return the list of possible bags to assign.
     */
    public ArrayList<CSBag> bags()
    {
        return possibleBags;
    }
    
    /**
     * Removes all possible bags from the possibly assignable bags list.
     */
    public void removeAllBags()
    {
        possibleBags = new ArrayList<CSBag>();
    }

    /**
     * 
     * @param o
     * @return 
     */
    @Override
    public int compareTo(CSItem i) 
    {
        if (this.numBags() < i.numBags())
        {
            return 1;
        }
        else if (this.numConstraints() > i.numConstraints())
        {
            return 1;
        }
        else if (this.degree() > i.degree())
        {
            return 1;
        }
        else if (this.size() > i.size())
        {
            return 1;
        }
        else if (this.size() == i.size())
        {
            return 0;
        }
        else
        {
            return -1;
        }
    }
}

