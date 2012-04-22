import java.util.*;

/**
 * Represents a bag in the grocery bagging problem.
 * 
 * @author Quincy Bowers  quincybowers@u.boisestate.edu
 * @version 1.0
 */
public class CSBag
{
    private ArrayList<CSItem> items;
    private HashSet<CSItem> constraints;
    
    /// The amount of space this bag has to store items.
    private int capacity;
    
    /// The total amount of space used by items packed in this bag.
    private int size;
    private String name;
    
    /**
     * Constructor.
     * 
     * @param name the name of the bag.
     * @param size the maximum size of this bag.
     */
    public CSBag(String name, int size)
    {
        this.name = name;
        this.capacity = size;
        this.size = 0;
        items = new ArrayList<CSItem>(size);
        constraints = new HashSet<CSItem>();
    }
    
    /**
     * Get the name of the bag.
     * 
     * @return the name of the bag.
     */
    public String name()
    {
        return name;
    }
    
    /**
     * Retrieve the total amount of space this bag has.
     * 
     * @return the total amount of space of this bag.
     */
    public int capacity()
    {
        return capacity;
    }
    
    /**
     * Retrieve the amount of free space this bag has.
     * 
     * @return the amount of free space for storing items in the bag.
     */
    public int spaceLeft()
    {
        return capacity - size;
    }
    
    /**
     * Check if the bag is full.
     * 
     * @return true if the bag is full; false otherwise.
     */
    public boolean isFull()
    {
        if (size == capacity)
        {
            return true;
        }
        
        return false;
    }
    
    /**
     * 
     * @return
     */
    public ArrayList<CSItem> items()
    {
        return items;
    }
    
    /**
     * Pack an item in this bag.
     * 
     * This method will pack an item into the bag and then check to see if the 
     * resulting state of the csp is unsolvable.  If so it will roll back the
     * changes and throw an exception.
     * 
     * @param item the item to pack.
     * 
     * @throws NoSpaceInBag if the item can't be packed because there is no room in the bag.
     * @throws ItemConstrained if the item can't be packed because it violates a constraint.
     * @throws SubtreeUnsolvable if the resulting state of the csp is no longer solvable.
     */
    public void pack(CSItem item) throws NoSpaceInBag, ItemConstrained, SubtreeUnsolvable
    {
        if (spaceLeft() < item.size()) { throw new NoSpaceInBag(name); }
        
        for (CSItem curritem : items)
        {
            if (!(curritem.packsWith(item) && item.packsWith(curritem)))
            {
                throw new ItemConstrained(item.name());
            }
        }
        
        items.add(item);
        size += item.size();
        constraints.addAll(item.getConstraints());
        
        try
        {
            item.pack(this);
        }
        catch (SubtreeUnsolvable e)
        {
            items.remove(item);
            size -= item.size();
            throw e;
        }
    }
    
    /**
     * Unpack an item from the bag.
     * 
     * @param item the item to unpack.
     */
    public void unpack(CSItem item)
    {
        items.remove(item);
        size -= item.size();

        constraints.clear();

        for (CSItem packed : items)
        {
            constraints.addAll(packed.getConstraints());
        }

        item.unpack();
    }
    
    /**
     * Check if an item can be packed in the bag.
     * 
     * @param newItem the item to check.
     * 
     * @return true if newItem can be packed in the bag.
     */
    public boolean canPack(CSItem newItem)
    {
        if (spaceLeft() < newItem.size()) { return false; }

        if (constraints.contains(newItem))
        {
            return false;
        }
//        for (CSItem item : items)
//        {
//            if (!(item.packsWith(newItem) && newItem.packsWith(item)))
//            {
//                return false;
//            }
//        }
        return true;
    }
    
    /**
     * 
     * @return
     */
    public boolean isEmpty()
    {
        if (items.size() == 0)
        {
            return true;
        }
        return false;
    }
    
    /**
     * Returns a string representation of what is packed in this bag.
     * 
     * @return the string describing what is packed in the bag.
     */
    public String packString()
    {
        String str = "";
        
        for (CSItem i : items)
        {
            str = str.concat(i.name() + '\t');
        }
        
        return str;
    }
    
    /**
     * @return a string representation of this object.
     */
    @Override
    public String toString()
    {
        String str = name + " " + size + "/" + capacity + " [";
        
        for (CSItem i : items)
        {
            str = str.concat(i.name() + " ");
        }
        
        return str.concat("]");
    }
}
