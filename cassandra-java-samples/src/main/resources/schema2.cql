CREATE TYPE basic_info (
  birthday timestamp,
  nationality text,
  weight text,
  height text
);
CREATE TABLE cyclist_stats(
 id bigint PRIMARY KEY,
 lastname text,
 basics FROZEN<basic_info>
)
WITH cdc=true;

INSERT INTO cyclist_stats (id, lastname, basics) VALUES (
  12341,
  'FRAME',
  { birthday : '1993-06-18', nationality : 'New Zealand', weight : '70kg', height : '5.6ft' }
);

update cyclist_stats set
basics={ birthday : '1993-06-18', nationality : 'New Zealand', weight : '70kg', height : '5.5ft' }
where id=12341 ;

