#!/usr/bin/perl

use strict;
use warnings;

my $SPACE = q{ };
my $num_tests  = shift @ARGV;
my %kills_for  = ();
my %wins_for   = ();
my %deaths_for = ();
my %points_for = ();

foreach my $test (1..$num_tests)
{
    print $test, "\n";
    my $test_cmd = join $SPACE, @ARGV;

    open(my $TEST, '-|', $test_cmd)
        or warn "Unable to execute test $test.  $!\n";

    while (my $line = <$TEST>)
    {
        if ($line =~ m{(.*):\skills\s=\s(\d+),\sdeaths\s=\s(\d+)}xms)
        {
            my $player = $1;
            my $kills  = $2;
            my $deaths = $3;

            $kills_for{$player}  += $kills;
            $deaths_for{$player} += $deaths;
            $wins_for{$player}   += ($deaths == 0) ? 1 : 0;
            $points_for{$player} += ($deaths == 0) ? 4 : 0;
            $points_for{$player} += $kills_for{$player} + $wins_for{$player};
        }
    }

    close $TEST;
}

print "TEST RESULTS\n";
print "------------\n";
printf "%28s %10s %10s %10s %10s\n", "PLAYER", "KILLS", "WINS", "POINTS", "DEATHS";

foreach my $player (sort by_points keys %points_for)
{
    printf "%28s %10d %10d %10d %10d\n", 
           $player, 
           $kills_for{$player},
           $wins_for{$player},
           $points_for{$player},
           $deaths_for{$player};
}

exit 0;

sub by_points
{
    return $points_for{$b} <=> $points_for{$a};
}

__END__

