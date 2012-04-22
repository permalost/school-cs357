import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.Map.Entry;

// ConstraintsSatisfier.java

/**
 * A constraints satisfaction solver for the bagging problem.
 *
 * The bagging problem consists of a set of items that must be packed into a set
 * of bags.  But each items may have constraints regarding what it can be packed
 * with.  The items each have a size and the bags each have a capacity.
 *
 * Formally, the problem is defined as a constraints satisfaction problem with
 * the triple <X, D, C>, X being a set of variables, D being the domains of
 * possible values for each X, and C being the constraints for each item.  For
 * this problem we are setting X to the list of items and D to the list of bags.
 * The problem then, is finding a bag assignment for each item such that the
 * item's constraints are satisfied, and no bag is packed beyond its capacity.
 *
 * Each grocery item to be bagged keeps a reference to every other item to be
 * bagged.  Each item also contains a list of bags it could be packed into.
 * This list contains references to the bags.  So if a bag is no longer valid
 * for an item we can just remove the bag from that item's bag list.
 *
 * @author Quincy Bowers  quincybowers@u.boisestate.edu
 * @version 1.0
 */
public class ConstraintsSatisfier
{
    private ArrayList<CSItem> items;
    private ArrayList<CSBag> bags;

    public static void main(String[] args)
    {
        // Validate arguments
        if (args.length < 1)
        {
            printUsage();
            System.exit(1);
        }

        // Open the input file
        File inputFile = new File(args[0]);
        Scanner s = null;

        try
        {
            s = new Scanner(inputFile);
        }
        catch (FileNotFoundException e)
        {
            System.err.println("File not found!");
            System.exit(1);
        }

        // Create objects for each of the items and save the constraints
        ArrayList<CSItem> items = new ArrayList<CSItem>();
        HashMap<CSItem,List<String>> constraints
            = new HashMap<CSItem,List<String>>();

        int num_bags         = Integer.parseInt(s.nextLine());
        int max_bag_size     = Integer.parseInt(s.nextLine());
        int total_bag_space  = num_bags * max_bag_size;
        int total_item_space = 0;

        ArrayList<CSBag> bags = new ArrayList<CSBag>(num_bags);
        for (int i=1; i<=num_bags; ++i)
        {
            bags.add(new CSBag("bag"+i, max_bag_size));
        }

        try
        {
            while (s.hasNextLine())
            {
                String[] fields = s.nextLine().split("\\s+");

                if (Integer.parseInt(fields[1]) > max_bag_size)
                {
                    throw new UnsolvableConstraintsProblem(
                            fields[0]
                          + " has size "
                          + fields[1]
                          + " but maximum bag size is "
                          + max_bag_size + "."
                    );
                }

                total_item_space += Integer.parseInt(fields[1]);
                if (total_item_space > total_bag_space)
                {
                    throw new UnsolvableConstraintsProblem("failure");
                }

                items.add(new CSItem(fields[0], Integer.parseInt(fields[1]), bags));

                if (fields.length > 2)
                {
                    List<String> list = Arrays.asList(fields);

                    constraints.put(items.get(items.size()-1), list.subList(2, fields.length));
                }
            }
        }
        catch (UnsolvableConstraintsProblem e)
        {
            unsolvable(e.getMessage());
        }

        // Now we work out all of the constraints for each item so that each has
        // a list of only negative constraints, what it can't be packed with.
        Set<Entry<CSItem, List<String>>> cset = constraints.entrySet();
        Iterator<Entry<CSItem, List<String>>> csetIter = cset.iterator();
        Entry<CSItem, List<String>> constraint;

        while (csetIter.hasNext())
        {
            constraint = csetIter.next();
            CSItem item = constraint.getKey();
            List<String> clist = constraint.getValue();
            item.initializeConstraints(items, clist);
        }

        ConstraintsSatisfier csp = new ConstraintsSatisfier(items, bags);

        // Start recursive backtracking search for a solution.
        String result = backtrackSearch(csp);

        if (result.equals("success"))
        {
            System.out.println("success");

            for (CSBag bag : csp.bags)
            {
                System.out.println(bag.packString());
            }
        }
        else
        {
            unsolvable(result);
        }
    }

    /**
     * Constructor
     *
     * @param items
     * @param bags
     */
    public ConstraintsSatisfier(ArrayList<CSItem> items, ArrayList<CSBag> bags)
    {
        this.items = items;
        this.bags = bags;
    }

    /**
     * Check if all items have been assigned a bag.
     *
     * @return true if all items have been assigned; false otherwise.
     */
    public boolean assignmentComplete()
    {
        for (CSItem item : items)
        {
            if (item.bag() == null)
            {
                return false;
            }

            next_sibling:
            for (CSItem sibling : item.bag().items())
            {
                if (sibling == item) { continue next_sibling; }

                if (!item.packsWith(sibling))
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Do a depth first backtracking search for a valid assignment of items to
     * bags.
     *
     * @param csp an instance of a ConstraintsSatisfier object.
     *
     * @return the String "success" if a solution was found;  any other return
     *         value constitutes a failure.
     */
    public static String backtrackSearch(ConstraintsSatisfier csp)
    {
        boolean backtracked = false;
        CSPIterator iter;
        Stack<CSPIterator> iterStack = new Stack<CSPIterator>();
        Stack<AbstractMap.SimpleEntry<CSBag, CSItem>> assignStack
            = new Stack<AbstractMap.SimpleEntry<CSBag, CSItem>>();

        next_level:
        for (int level=0; level<csp.items.size(); ++level)
        {
            if (backtracked)
            {
                iter = iterStack.pop();
                AbstractMap.SimpleEntry<CSBag, CSItem> last = assignStack.pop();
                last.getKey().unpack(last.getValue());
                csp.rebuildDomains();
                backtracked = false;
            }
            else
            {
                // Get the next item to assign to
                iter = csp.new CSPIterator();
            }

            while (iter.hasNext())
            {
                CSItem item = iter.next();

                boolean triedEmptyBag = false;

                next_bag:
                for (int b=0; b<item.numBags(); ++b)
                {
                    CSBag bag = item.getBag(b);

                    // If there is more than one empty bag we will only try the
                    // first one.  It doesn't make sense to try more than one
                    // empty bag since it will not effect the solution.
                    if (bag.isEmpty() && triedEmptyBag)
                    {
                        continue next_bag;
                    }
                    else if (bag.isEmpty() && !triedEmptyBag)
                    {
                        triedEmptyBag = true;
                    }

                    try
                    {
                        bag.pack(item);
                    }
                    catch (NoSpaceInBag e)      { continue next_bag; }
                    catch (ItemConstrained e)   { continue next_bag; }
                    catch (SubtreeUnsolvable e) { continue next_bag; }

                    // Check if this assignment is a solution.
                    if (csp.assignmentComplete()) { return "success"; }

                    // Try to reduce the possible bags for each item.
                    if (csp.reduceDomains())
                    {
                        iterStack.push(iter);
                        assignStack.push(
                            new AbstractMap.SimpleEntry<CSBag, CSItem>(bag, item));
                        continue next_level;
                    }
                    else
                    {
                        // If the problem became unsolvable, we'll rebuild the domains
                        // and move onto the next iteration.
                        bag.unpack(item);
                        csp.rebuildDomains();
                    }
                }
            }

            // We have to backtrack
            if (level > 0)
            {
                backtracked = true;
                level -= 2;
            }
            else
            {
                return "failure";
            }
        }

        return "failure";
    }

    /**
     *
     * @param item
     */
    public boolean forwardCheck(CSItem item)
    {
        for (CSItem constraint : item.getConstraints())
        {
            if (constraint.hasBag(item.bag()))
            {
                constraint.removeBag(item.bag());
            }

            if (constraint.numBags() == 0)
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Examines the assignments on each item and removes non-assignable bags.
     *
     * @return true if the domains were reduced; false if an unsolvable problem
     *              resulted from this operation.
     */
    public boolean reduceDomains()
    {
        for (CSItem item : items)
        {
            if (item.assigned())
            {
                item.removeAllBags();
            }
            else
            {
                ArrayList<CSBag> toRemove = new ArrayList<CSBag>();
                for (CSBag bag : item.bags())
                {
                    if (!bag.canPack(item))
                    {
                        toRemove.add(bag);
                    }
                }

                for (CSBag bag : toRemove)
                {
                    item.removeBag(bag);
                }

                if (item.numBags() == 0)
                {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Ensures that all possible bag assignments are available to each item.
     */
    public void rebuildDomains()
    {
        for (CSItem item : items)
        {
            for (CSBag bag : bags)
            {
                if (bag.canPack(item))
                {
                    item.addBag(bag);
                }
            }
        }
    }

    /** Prints a message explaining why this problem is unsolvable and exits.
     *
     * @param msg the explanation.
     */
    public static void unsolvable(String msg)
    {
        System.out.print("failure");
        System.exit(0);
    }

    /**
     * Prints a usage message.
     */
    private static void printUsage()
    {
        System.err.println("Usage: $ java ConstraintsSatisfier [file]");
    }

    /**
     * Returns an Iterator over the items in the items list of the csp.
     *
     * This iterator returns items in an order using the MRV heuristic.  In the
     * event of a tie we pick the larger value.
     *
     * @author Quincy Bowers quincybowers@u.boisesate.edu
     * @version 1.0
     */
    private class CSPIterator implements Iterator<CSItem>
    {
        ArrayList<CSItem> list;
        int index;

        public CSPIterator()
        {
            list = sortItems();
            if (list.size() > 0)
            {
                index = 0;
            }
            else
            {
                index = -1;
            }
        }

        @Override
        public boolean hasNext()
        {
            if (index >= 0 && index < list.size())
            {
                return true;
            }
            return false;
        }

        @Override
        public CSItem next()
        {
            if (hasNext())
            {
                CSItem item = list.get(index);
                ++index;
                return item;
            }
            return null;
        }

        @Override
        public void remove() { /* Intentionally empty. */ }

        /**
         * Sorts the items in the order that will be returned by the iterator.
         *
         * This method is currently implemented with mergsort.  It could easily
         * be updated with any sort method.
         *
         * Before the sorted list is returned, any item with an assigned bag is
         * removed from the list.
         *
         * @return
         */
        private ArrayList<CSItem> sortItems()
        {
            ArrayList<CSItem> sorted = mergeSort(items);

            for (int i=sorted.size()-1; i>=0; --i)
            {
                if (sorted.get(i).assigned())
                {
                    sorted.remove(i);
                }
            }

            return sorted;
        }

        /**
         * Perform a mergesort on the items in the csp.
         *
         * @param orig the original list of items to sort.
         *
         * @return the sorted list.
         */
        private ArrayList<CSItem> mergeSort(ArrayList<CSItem> orig)
        {
            if (orig.size() <= 1) { return orig; }

            ArrayList<CSItem> left  = new ArrayList<CSItem>();
            ArrayList<CSItem> right = new ArrayList<CSItem>();

            int middle = orig.size() / 2;

            for (int i=0; i<orig.size(); ++i)
            {
                if (i<middle)
                {
                    left.add(orig.get(i));
                }
                else
                {
                    right.add(orig.get(i));
                }
            }

            left  = mergeSort(left);
            right = mergeSort(right);

            return merge(left, right);
        }

        /**
         * Perform the merge step of mergesort on two lists.
         *
         * The items are sorted such that items with more constraints come
         * before those with fewer.  If there is a tie the item with the larger
         * size comes first.  If there is another tie than they are equivalent
         * and their relative order is preserved.
         *
         * @param left
         * @param right
         *
         * @return
         */
        private ArrayList<CSItem> merge(ArrayList<CSItem> left, ArrayList<CSItem> right)
        {
            ArrayList<CSItem> result = new ArrayList<CSItem>();

            while (left.size() > 0 || right.size() > 0)
            {
                if (left.size() > 0 && right.size() > 0)
                {
                    if (left.get(0).compareTo(right.get(0)) >= 0)
                    {
                        result.add(left.remove(0));
                    }
                    else
                    {
                        result.add(left.remove(0));
                    }
                }
                else if (left.size() > 0)
                {
                    result.add(left.remove(0));
                }
                else if (right.size() > 0)
                {
                    result.add(right.remove(0));
                }
            }

            return result;
        }
    }
}
