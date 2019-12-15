package org.nkumar.ae.model;

@FunctionalInterface
public interface Allocator
{
    /**
     * Try to allocate at most count items for the passed gender and shape.
     * The count could be negative, in which case the gender and shape is already over allocated.
     * Return the number that was actually allocate.
     * @return count that could be allocated
     */
    int allocate(Gender gender, String shape, int count);
}
