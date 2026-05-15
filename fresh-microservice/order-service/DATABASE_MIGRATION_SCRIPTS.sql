# SQL Migration Scripts for price_at_purchase Field

## If you need to manually fix the database schema:

### Option 1: Update Existing Column (Recommended)

```sql
-- Connect to your database
USE booknest_order_service_db;

-- Check current column definition
DESCRIBE order_items;

-- Update the column to have DEFAULT NULL
ALTER TABLE order_items
MODIFY COLUMN price_at_purchase DECIMAL(10, 2) DEFAULT NULL;

-- Verify the change
DESCRIBE order_items;
```

### Option 2: Drop and Recreate Column

```sql
-- If the column doesn't exist yet:
ALTER TABLE order_items
ADD COLUMN price_at_purchase DECIMAL(10, 2) DEFAULT NULL;

-- If you need to completely recreate it:
ALTER TABLE order_items
DROP COLUMN price_at_purchase;

ALTER TABLE order_items
ADD COLUMN price_at_purchase DECIMAL(10, 2) DEFAULT NULL;
```

### Option 3: Complete Schema Reset (Development Only)

**⚠️ WARNING: This will delete all data!**

```sql
-- Drop and recreate the tables from scratch
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;

-- Restart your Spring Boot application
-- It will recreate tables with proper schema
```

## Verification Scripts

```sql
-- Check if column exists and its definition
SELECT COLUMN_NAME, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'order_items'
AND COLUMN_NAME = 'price_at_purchase';

-- Expected Output:
-- COLUMN_NAME: price_at_purchase
-- IS_NULLABLE: YES
-- COLUMN_DEFAULT: NULL
-- COLUMN_TYPE: decimal(10,2)

-- View all columns in order_items table
DESCRIBE order_items;

-- View all columns in orders table
DESCRIBE orders;
```

## Troubleshooting

### Issue: "Referencing column 'order_id' and referenced column 'order_id' in foreign key constraint are incompatible"

**Solution**:
```sql
-- Check foreign key constraints
SELECT CONSTRAINT_NAME, TABLE_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_NAME = 'order_items'
AND REFERENCED_TABLE_NAME = 'orders';

-- If needed, drop and recreate the foreign key
ALTER TABLE order_items DROP FOREIGN KEY FKbioxgbv59vetrxe0ejfubep1w;

ALTER TABLE order_items
ADD CONSTRAINT fk_order_items_order_id
FOREIGN KEY (order_id) REFERENCES orders(order_id);
```

### Issue: "Data truncated for column 'status' at row 1"

**Solution**: Check enum values:
```sql
-- View current status values
SELECT DISTINCT status FROM orders;

-- Valid enum values should be: PENDING_PAYMENT, CONFIRMED, FAILED, CANCELLED
-- Update any invalid values
UPDATE orders SET status = 'PENDING_PAYMENT' WHERE status NOT IN ('PENDING_PAYMENT', 'CONFIRMED', 'FAILED', 'CANCELLED');
```

## Monitoring & Health Check

```sql
-- Check table structure integrity
SELECT TABLE_NAME, ENGINE, TABLE_ROWS, DATA_FREE
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'booknest_order_service_db'
AND TABLE_NAME IN ('orders', 'order_items');

-- Count records
SELECT 'orders' as table_name, COUNT(*) as record_count FROM orders
UNION
SELECT 'order_items' as table_name, COUNT(*) as record_count FROM order_items;

-- Check for orphaned order_items (items without orders)
SELECT * FROM order_items oi
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.order_id = oi.order_id);
```

## Backup Before Making Changes

```sql
-- Create backup tables
CREATE TABLE orders_backup AS SELECT * FROM orders;
CREATE TABLE order_items_backup AS SELECT * FROM order_items;

-- You can restore with:
-- TRUNCATE orders;
-- INSERT INTO orders SELECT * FROM orders_backup;
```

---

## Integration with Hibernate

Once you fix the database manually, Hibernate will:
1. Recognize the corrected schema
2. No longer attempt to modify it (if using `ddl-auto=validate`)
3. Work correctly for all future operations

**Note**: It's better to let Hibernate handle schema creation/updates automatically. Only use these scripts if:
- Manual intervention is required
- Database is in an inconsistent state
- You need to preserve existing data during migration

---

**Last Updated**: April 18, 2026
**Database**: MySQL 8.0+
**Application**: Spring Boot 4.0.5 with Hibernate 7.2.7

